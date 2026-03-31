package com.compilers.model;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

public class LRItem {
    final String      lhs;
    final String[]    rhs;
    final int         dot;
    final Set<String> lookahead;

    public LRItem(String lhs, String[] rhs, int dot, Set<String> lookahead) {
        this.lhs       = lhs;
        this.rhs       = rhs;
        this.dot       = dot;
        this.lookahead = lookahead;
    }

    public LRItem getCore()            { return new LRItem(lhs, rhs, dot, null); }
    public String getLhs()             { return lhs; }
    public String[] getRhs()           { return rhs; }
    public int getDotPosition()        { return dot; }
    public Set<String> getLookahead()  { return lookahead; }

    // Equality is intentionally core-only (lhs + rhs + dot, no lookahead)
    // so that states with the same core can be identified for LALR merging.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LRItem)) return false;
        LRItem other = (LRItem) o;
        return dot == other.dot
                && Objects.equals(lhs, other.lhs)
                && Arrays.equals(rhs, other.rhs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lhs, dot) * 31 + Arrays.hashCode(rhs);
    }
}
