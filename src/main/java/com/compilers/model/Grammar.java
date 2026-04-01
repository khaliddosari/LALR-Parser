package com.compilers.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a context-free grammar.
 * Automatically augments the grammar by prepending the production S' -> startSymbol
 * at index 0, so production indices align with reduce action numbers used by LALRParser.
 */
public class Grammar {

    public static class Production {
        public final String   lhs;
        public final String[] rhs; // empty array = ε production

        public Production(String lhs, String[] rhs) {
            this.lhs = lhs;
            this.rhs = rhs;
        }

        public String toRuleString() {
            return rhs.length == 0
                    ? lhs + " -> "
                    : lhs + " -> " + String.join(" ", rhs);
        }
    }

    public final List<Production> productions;    // index 0 = augmented S' -> startSymbol
    public final Set<String>      nonterminals;   // all LHS symbols
    public final Set<String>      terminals;      // RHS symbols that are not nonterminals

    public Grammar(String startSymbol, List<Production> userProductions) {
        String augmentedStart = startSymbol + "'";

        productions = new ArrayList<>();
        productions.add(new Production(augmentedStart, new String[]{startSymbol}));
        productions.addAll(userProductions);

        // Nonterminals = every symbol that appears as a LHS
        nonterminals = new LinkedHashSet<>();
        for (Production p : productions) nonterminals.add(p.lhs);

        // Terminals = symbols that appear in some RHS but are never a LHS,
        // plus "$" (end-of-input marker) which never appears in any production
        // but must be part of the terminal set so ACTION table handling is uniform.
        terminals = new LinkedHashSet<>();
        for (Production p : productions)
            for (String sym : p.rhs)
                if (!nonterminals.contains(sym)) terminals.add(sym);
        terminals.add("$");
    }

    /** Returns one rule string per production, indexed to match reduce action numbers. */
    public List<String> ruleStrings() {
        List<String> rules = new ArrayList<>();
        for (Production p : productions) rules.add(p.toRuleString());
        return rules;
    }
}
