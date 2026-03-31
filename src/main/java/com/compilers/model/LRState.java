package com.compilers.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LRState {
    final int         id;
    final Set<LRItem> items;

    public LRState(int id, Set<LRItem> items) {
        this.id    = id;
        this.items = new HashSet<>(items);
    }

    public int         getStateId() { return id; }
    public Set<LRItem> getItems()   { return items; }

    public Set<LRItem> getCore() {
        Set<LRItem> core = new HashSet<>();
        for (LRItem item : items) core.add(item.getCore());
        return core;
    }

    public boolean hasSameCore(LRState other) {
        return getCore().equals(other.getCore());
    }

    public LRState merge(LRState other) {
        if (!hasSameCore(other))
            throw new IllegalArgumentException("Cannot merge states with different cores");

        Map<LRItem, Set<String>> merged = new HashMap<>();
        for (LRItem item : items)
            merged.put(item.getCore(), new HashSet<>(item.getLookahead()));
        for (LRItem item : other.items)
            merged.computeIfAbsent(item.getCore(), k -> new HashSet<>())
                  .addAll(item.getLookahead());

        Set<LRItem> result = new HashSet<>();
        for (Map.Entry<LRItem, Set<String>> entry : merged.entrySet()) {
            LRItem core = entry.getKey();
            result.add(new LRItem(core.getLhs(), core.getRhs(),
                                  core.getDotPosition(), entry.getValue()));
        }
        return new LRState(Math.min(id, other.id), result);
    }
}
