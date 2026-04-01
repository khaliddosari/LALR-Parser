package com.compilers.parser;

import com.compilers.model.LALRResult;
import com.compilers.model.LRItem;
import com.compilers.model.LRState;
import com.compilers.model.ParsingTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LALRConstructor {
    final List<LRState>         canonicalStates;
    final ParsingTable          canonicalTable;
    final Map<Integer, Integer> stateMapping = new HashMap<>();

    List<LRState> lalrStates;
    ParsingTable  lalrTable;

    public LALRConstructor(List<LRState> canonicalStates, ParsingTable canonicalTable) {
        this.canonicalStates = canonicalStates;
        this.canonicalTable  = canonicalTable;
    }

    public LALRResult buildResult() {
        List<String> mergeLog = new ArrayList<>();

        Map<Set<LRItem>, List<LRState>> groups = new LinkedHashMap<>();

        LRState initialState = null;
        for (LRState state : canonicalStates)
            if (state.getStateId() == 0) { initialState = state; break; }
        if (initialState != null)
            groups.put(initialState.getCore(),
                       new ArrayList<>(Collections.singletonList(initialState)));

        for (LRState state : canonicalStates) {
            if (state.getStateId() == 0) continue;
            groups.computeIfAbsent(state.getCore(), k -> new ArrayList<>()).add(state);
        }

        lalrStates = new ArrayList<>();
        int lalrId = 0;
        for (Map.Entry<Set<LRItem>, List<LRState>> entry : groups.entrySet()) {
            List<LRState> group = entry.getValue();
            LRState merged = group.get(0);
            for (int i = 1; i < group.size(); i++) merged = merged.merge(group.get(i));
            lalrStates.add(new LRState(lalrId, merged.getItems()));
            for (LRState s : group) stateMapping.put(s.getStateId(), lalrId);
            if (group.size() > 1) {
                String ids = group.stream()
                        .map(s -> String.valueOf(s.getStateId()))
                        .reduce((a, b) -> a + ", " + b).orElse("");
                mergeLog.add("Merged states " + ids + " into LALR state " + lalrId);
            }
            lalrId++;
        }

        lalrTable = new ParsingTable(lalrStates.size(),
                canonicalTable.getTerminals(), canonicalTable.getNonterminals());

        for (LRState canonState : canonicalStates) {
            int canonId     = canonState.getStateId();
            int lalrStateId = stateMapping.get(canonId);
            for (String terminal : canonicalTable.getTerminals()) {
                String act = canonicalTable.getAction(canonId, terminal);
                if (act == null) continue;
                if (act.startsWith("s")) {
                    int target = Integer.parseInt(act.substring(1));
                    lalrTable.setAction(lalrStateId, terminal, "s" + stateMapping.get(target));
                } else {
                    lalrTable.setAction(lalrStateId, terminal, act);
                }
            }
        }

        for (LRState canonState : canonicalStates) {
            int canonId     = canonState.getStateId();
            int lalrStateId = stateMapping.get(canonId);
            for (String nt : canonicalTable.getNonterminals()) {
                Integer target = canonicalTable.getGoto(canonId, nt);
                if (target != null)
                    lalrTable.setGoto(lalrStateId, nt, stateMapping.get(target));
            }
        }

        List<String> conflicts = lalrTable.checkConflicts();

        return new LALRResult(
                canonicalStates.size(),
                lalrStates.size(),
                mergeLog,
                conflicts,
                new LALRResult.TableData(lalrTable));
    }

    public ParsingTable getLalrTable() { return lalrTable; }
}
