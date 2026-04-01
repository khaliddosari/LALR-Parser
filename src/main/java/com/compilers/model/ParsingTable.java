package com.compilers.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class ParsingTable {
    final Map<Integer, Map<String, String>>  action;
    final Map<Integer, Map<String, Integer>> goTo;
    final Set<String> terminals;
    final Set<String> nonterminals;
    final int         numStates;

    // Conflicts are collected here as actions are set, so checkConflicts()
    // actually has something to report.
    private final List<String> conflicts = new ArrayList<>();

    public ParsingTable(int numStates, Set<String> terminals, Set<String> nonterminals) {
        this.numStates    = numStates;
        this.terminals    = new HashSet<>(terminals);
        this.nonterminals = new HashSet<>(nonterminals);
        action = new HashMap<>();
        goTo   = new HashMap<>();
        for (int i = 0; i < numStates; i++) {
            action.put(i, new HashMap<>());
            goTo.put(i, new HashMap<>());
        }
    }

    public void setAction(int state, String symbol, String newAction) {
        String existing = action.get(state).get(symbol);
        if (existing != null && !existing.equals(newAction)) {
            conflicts.add(String.format(
                    "Conflict at state %d on '%s': %s vs %s",
                    state, symbol, existing, newAction));
        }
        action.get(state).put(symbol, newAction);
    }

    public void setGoto(int state, String nonterminal, int target) {
        goTo.get(state).put(nonterminal, target);
    }

    public String  getAction(int state, String symbol)     { return action.get(state).get(symbol); }
    public Integer getGoto(int state, String nonterminal)  { return goTo.get(state).get(nonterminal); }
    public int         getNumStates()                      { return numStates; }
    public Set<String> getTerminals()                      { return terminals; }
    public Set<String> getNonterminals()                   { return nonterminals; }
    public List<String> checkConflicts()                   { return conflicts; }

    public void printTable() {
        System.out.println("\n=== PARSING TABLE ===\n");

        Set<String> allTerminals = new TreeSet<>(terminals); // $ already included

        System.out.println("ACTION TABLE:");
        System.out.print("State\t");
        for (String t : allTerminals) System.out.print(t + "\t");
        System.out.println();
        for (int state = 0; state < numStates; state++) {
            System.out.print(state + "\t");
            for (String t : allTerminals) {
                String a = getAction(state, t);
                System.out.print((a != null ? a : "") + "\t");
            }
            System.out.println();
        }

        System.out.println("\nGOTO TABLE:");
        System.out.print("State\t");
        for (String nt : new TreeSet<>(nonterminals)) System.out.print(nt + "\t");
        System.out.println();
        for (int state = 0; state < numStates; state++) {
            System.out.print(state + "\t");
            for (String nt : new TreeSet<>(nonterminals)) {
                Integer g = getGoto(state, nt);
                System.out.print((g != null ? g : "") + "\t");
            }
            System.out.println();
        }
    }
}
