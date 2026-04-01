package com.compilers.model;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.LinkedHashMap;

public class LALRResult {
    private final int canonicalStateCount;
    private final int lalrStateCount;
    private final List<String> mergeLog;
    private final List<String> conflicts;
    private final boolean isLALR;
    private final TableData tableData;

    public LALRResult(int canonicalStateCount, int lalrStateCount,
                      List<String> mergeLog, List<String> conflicts, TableData tableData) {
        this.canonicalStateCount = canonicalStateCount;
        this.lalrStateCount      = lalrStateCount;
        this.mergeLog            = mergeLog;
        this.conflicts           = conflicts;
        this.isLALR              = conflicts.isEmpty();
        this.tableData           = tableData;
    }

    public int getCanonicalStateCount()  { return canonicalStateCount; }
    public int getLalrStateCount()       { return lalrStateCount; }
    public List<String> getMergeLog()    { return mergeLog; }
    public List<String> getConflicts()   { return conflicts; }
    public boolean isLALR()              { return isLALR; }
    public TableData getTableData()      { return tableData; }

    public static class TableData {
        private final Set<String> terminals;
        private final Set<String> nonterminals;
        private final Map<Integer, Map<String, String>> actionTable;
        private final Map<Integer, Map<String, Integer>> gotoTable;

        public TableData(ParsingTable table) {
            this.terminals    = new TreeSet<>(table.getTerminals());
            this.nonterminals = new TreeSet<>(table.getNonterminals());
            this.actionTable  = new TreeMap<>();
            this.gotoTable    = new TreeMap<>();

            for (int s = 0; s < table.getNumStates(); s++) {
                Map<String, String> actionRow = new LinkedHashMap<>();
                for (String t : terminals) {
                    String act = table.getAction(s, t);
                    if (act != null) actionRow.put(t, act);
                }
                if (!actionRow.isEmpty()) actionTable.put(s, actionRow);

                Map<String, Integer> gotoRow = new LinkedHashMap<>();
                for (String nt : nonterminals) {
                    Integer g = table.getGoto(s, nt);
                    if (g != null) gotoRow.put(nt, g);
                }
                if (!gotoRow.isEmpty()) gotoTable.put(s, gotoRow);
            }
        }

        public Set<String> getTerminals()                        { return terminals; }
        public Set<String> getNonterminals()                     { return nonterminals; }
        public Map<Integer, Map<String, String>> getActionTable()  { return actionTable; }
        public Map<Integer, Map<String, Integer>> getGotoTable()   { return gotoTable; }
    }
}
