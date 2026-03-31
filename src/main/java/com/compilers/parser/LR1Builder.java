package com.compilers.parser;

import com.compilers.model.Grammar;
import com.compilers.model.LRItem;
import com.compilers.model.LRState;
import com.compilers.model.ParsingTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

/**
 * Builds the canonical LR(1) item sets and parsing table from a Grammar.
 *
 * Pipeline:
 *   1. Compute FIRST sets for all grammar symbols.
 *   2. Build canonical LR(1) item sets via closure / goto.
 *   3. Construct ACTION and GOTO table entries from the item sets.
 *   4. Return a BuildResult for LALRConstructor to consume (merge same-core states).
 *
 * The resulting canonical states and table are structurally identical to what
 * was previously hand-coded in Main, but are now derived automatically from
 * the grammar rules.
 */
public class LR1Builder {

    // ── Internal LR(1) item ───────────────────────────────────────────────────
    // Lookahead is included in equals/hashCode so that items differing only in
    // lookahead are treated as distinct (required for correct LR(1) closure).

    private static class Item {
        final int    productionIndex;
        final int    dot;
        final String lookahead;

        Item(int productionIndex, int dot, String lookahead) {
            this.productionIndex = productionIndex;
            this.dot             = dot;
            this.lookahead       = lookahead;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Item)) return false;
            Item other = (Item) o;
            return productionIndex == other.productionIndex
                    && dot == other.dot
                    && Objects.equals(lookahead, other.lookahead);
        }

        @Override
        public int hashCode() {
            return Objects.hash(productionIndex, dot, lookahead);
        }
    }

    // ── Build result ──────────────────────────────────────────────────────────

    public static class BuildResult {
        public final List<LRState> canonicalStates;
        public final ParsingTable  canonicalTable;
        public final List<String>  rules; // rule strings for LALRParser

        BuildResult(List<LRState> canonicalStates, ParsingTable canonicalTable, List<String> rules) {
            this.canonicalStates = canonicalStates;
            this.canonicalTable  = canonicalTable;
            this.rules           = rules;
        }
    }

    // ── Fields ────────────────────────────────────────────────────────────────

    private final Grammar                grammar;
    private Map<String, Set<String>>     firstSets;

    public LR1Builder(Grammar grammar) {
        this.grammar = grammar;
    }

    // ── FIRST sets ────────────────────────────────────────────────────────────

    private void computeFirstSets() {
        firstSets = new HashMap<>();

        // Every terminal maps to {itself}; every nonterminal starts empty
        for (String t  : grammar.terminals)    firstSets.put(t,  new HashSet<>(Collections.singleton(t)));
        for (String nt : grammar.nonterminals) firstSets.put(nt, new HashSet<>());

        // Iterate until no new symbols are added to any FIRST set
        boolean changed = true;
        while (changed) {
            changed = false;
            for (Grammar.Production p : grammar.productions) {
                Set<String> target = firstSets.get(p.lhs);
                int before = target.size();

                if (p.rhs.length == 0) {
                    target.add("ε"); // ε-production
                } else {
                    boolean allNullable = true;
                    for (String sym : p.rhs) {
                        Set<String> fSym = firstSets.getOrDefault(sym,
                                new HashSet<>(Collections.singleton(sym)));
                        for (String s : fSym)
                            if (!s.equals("ε")) target.add(s);
                        if (!fSym.contains("ε")) { allNullable = false; break; }
                    }
                    if (allNullable) target.add("ε");
                }

                if (target.size() != before) changed = true;
            }
        }
    }

    /**
     * Returns FIRST(symbols[from..] followed by terminalAfter).
     * Never includes ε in the result because terminalAfter is always a
     * concrete terminal (e.g. "$").
     */
    private Set<String> firstOfSuffix(String[] symbols, int from, String terminalAfter) {
        Set<String> result = new HashSet<>();
        boolean allNullable = true;
        for (int i = from; i < symbols.length; i++) {
            Set<String> f = firstSets.getOrDefault(symbols[i],
                    new HashSet<>(Collections.singleton(symbols[i])));
            for (String s : f)
                if (!s.equals("ε")) result.add(s);
            if (!f.contains("ε")) { allNullable = false; break; }
        }
        if (allNullable) result.add(terminalAfter);
        return result;
    }

    // ── LR(1) closure ─────────────────────────────────────────────────────────

    private Set<Item> closure(Set<Item> kernel) {
        Set<Item>   result   = new HashSet<>(kernel);
        Queue<Item> worklist = new LinkedList<>(kernel);

        while (!worklist.isEmpty()) {
            Item item = worklist.poll();
            Grammar.Production prod = grammar.productions.get(item.productionIndex);

            if (item.dot >= prod.rhs.length) continue; // dot is at the end

            String nextSym = prod.rhs[item.dot];
            if (!grammar.nonterminals.contains(nextSym)) continue; // terminal after dot

            // For item [A -> α.Bβ, a], new lookaheads = FIRST(β a)
            Set<String> newLookaheads = firstOfSuffix(prod.rhs, item.dot + 1, item.lookahead);

            for (int i = 0; i < grammar.productions.size(); i++) {
                if (!grammar.productions.get(i).lhs.equals(nextSym)) continue;
                for (String la : newLookaheads) {
                    Item newItem = new Item(i, 0, la);
                    if (result.add(newItem)) worklist.add(newItem);
                }
            }
        }
        return result;
    }

    // ── LR(1) goto ────────────────────────────────────────────────────────────

    private Set<Item> goto_(Set<Item> state, String symbol) {
        Set<Item> kernel = new HashSet<>();
        for (Item item : state) {
            Grammar.Production prod = grammar.productions.get(item.productionIndex);
            if (item.dot < prod.rhs.length && prod.rhs[item.dot].equals(symbol))
                kernel.add(new Item(item.productionIndex, item.dot + 1, item.lookahead));
        }
        return kernel.isEmpty() ? Collections.emptySet() : closure(kernel);
    }

    // ── Canonical LR(1) collection + table ───────────────────────────────────

    public BuildResult build() {
        computeFirstSets();

        // Initial item: [S' -> .startSymbol, $]
        Set<Item> initial = closure(
                new HashSet<>(Collections.singleton(new Item(0, 0, "$"))));

        List<Set<Item>>            itemSets   = new ArrayList<>();
        Map<Set<Item>, Integer>    stateIndex = new HashMap<>();
        List<Map<String, Integer>> trans      = new ArrayList<>(); // stateId -> symbol -> stateId

        itemSets.add(initial);
        stateIndex.put(initial, 0);
        trans.add(new HashMap<>());

        Queue<Integer> worklist = new LinkedList<>();
        worklist.add(0);

        // Process symbols in sorted order so state numbering is deterministic
        Set<String> allSymbols = new TreeSet<>();
        allSymbols.addAll(grammar.terminals);
        allSymbols.addAll(grammar.nonterminals);

        while (!worklist.isEmpty()) {
            int sid = worklist.poll();
            for (String sym : allSymbols) {
                Set<Item> next = goto_(itemSets.get(sid), sym);
                if (next.isEmpty()) continue;

                Integer nextId = stateIndex.get(next);
                if (nextId == null) {
                    nextId = itemSets.size();
                    itemSets.add(next);
                    stateIndex.put(next, nextId);
                    trans.add(new HashMap<>());
                    worklist.add(nextId);
                }
                trans.get(sid).put(sym, nextId);
            }
        }

        // Convert item sets to LRStates
        List<LRState> lrStates = new ArrayList<>();
        for (int i = 0; i < itemSets.size(); i++)
            lrStates.add(toLRState(i, itemSets.get(i)));

        // Build parsing table
        ParsingTable table = new ParsingTable(
                lrStates.size(), grammar.terminals, grammar.nonterminals);

        for (int sid = 0; sid < itemSets.size(); sid++) {

            // Shift and Goto from transition table
            for (Map.Entry<String, Integer> e : trans.get(sid).entrySet()) {
                String sym    = e.getKey();
                int    target = e.getValue();
                if (grammar.terminals.contains(sym))
                    table.setAction(sid, sym, "s" + target);
                else
                    table.setGoto(sid, sym, target);
            }

            // Reduce and Accept from completed items (dot at end)
            for (Item item : itemSets.get(sid)) {
                Grammar.Production prod = grammar.productions.get(item.productionIndex);
                if (item.dot < prod.rhs.length) continue;

                if (item.productionIndex == 0) {
                    // Augmented production S' -> startSymbol, always accept on $
                    table.setAction(sid, "$", "accept");
                } else {
                    table.setAction(sid, item.lookahead, "r" + item.productionIndex);
                }
            }
        }

        return new BuildResult(lrStates, table, grammar.ruleStrings());
    }

    /**
     * Converts a set of LR(1) items into an LRState.
     * Items that share the same core (productionIndex + dot) are merged into
     * one LRItem whose lookahead set is the union of all their lookaheads.
     * This is necessary because LRItem.equals() ignores lookahead, so a plain
     * HashSet would silently drop duplicates otherwise.
     */
    private LRState toLRState(int id, Set<Item> items) {
        // Use insertion-order map so output is deterministic
        Map<String, Set<String>> lookaheadMap = new LinkedHashMap<>();
        Map<String, Item>        coreMap      = new LinkedHashMap<>();

        for (Item item : items) {
            String key = item.productionIndex + ":" + item.dot;
            lookaheadMap.computeIfAbsent(key, k -> new HashSet<>()).add(item.lookahead);
            coreMap.put(key, item);
        }

        Set<LRItem> lrItems = new HashSet<>();
        for (Map.Entry<String, Item> e : coreMap.entrySet()) {
            Item               item = e.getValue();
            Grammar.Production prod = grammar.productions.get(item.productionIndex);
            lrItems.add(new LRItem(prod.lhs, prod.rhs, item.dot,
                                   lookaheadMap.get(e.getKey())));
        }
        return new LRState(id, lrItems);
    }
}
