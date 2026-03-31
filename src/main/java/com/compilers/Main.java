package com.compilers;

import com.compilers.model.Grammar;
import com.compilers.model.ParsingTable;
import com.compilers.parser.LALRConstructor;
import com.compilers.parser.LALRParser;
import com.compilers.parser.LR1Builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("LALR PARSER IMPLEMENTATION");
        System.out.println("========================================\n");
        testSimpleGrammar();
        System.out.println("\n\n========================================\n\n");
        testExpressionGrammar();
    }

    // ── Simple grammar: S -> A a | b,  A -> c ────────────────────────────────

    static void testSimpleGrammar() {
        System.out.println("SIMPLE GRAMMAR\nGrammar:\n  S -> A a\n  S -> b\n  A -> c");

        Grammar grammar = new Grammar("S", Arrays.asList(
                new Grammar.Production("S", new String[]{"A", "a"}),
                new Grammar.Production("S", new String[]{"b"}),
                new Grammar.Production("A", new String[]{"c"})
        ));

        System.out.println("\n--- Function 1: LALR Table Construction ---");
        LR1Builder.BuildResult result = new LR1Builder(grammar).build();
        LALRConstructor constructor = new LALRConstructor(result.canonicalStates, result.canonicalTable);
        ParsingTable lalrTable = constructor.constructLALRTable();
        constructor.reportConflicts();
        lalrTable.printTable();

        System.out.println("\n--- Function 2: LALR Parsing ---");
        LALRParser parser = new LALRParser(lalrTable, result.rules);
        List<TestResult> results = new ArrayList<>();

        results.add(runTest(parser,  1, "c a",   true));
        results.add(runTest(parser,  2, "b",     true));
        results.add(runTest(parser,  3, "a b",   false));
        results.add(runTest(parser,  4, "c",     false));
        results.add(runTest(parser,  5, "a",     false));
        results.add(runTest(parser,  6, "c c",   false));
        results.add(runTest(parser,  7, "b a",   false));
        results.add(runTest(parser,  8, "c b",   false));
        results.add(runTest(parser,  9, "a a",   false));
        results.add(runTest(parser, 10, "b b",   false));
        results.add(runTest(parser, 11, "c a a", false));
        results.add(runTest(parser, 12, "",      false));
        results.add(runTest(parser, 13, "b c",   false));

        printSummary(results);
    }

    // ── Expression grammar: E -> E + T | T,  T -> id ─────────────────────────

    static void testExpressionGrammar() {
        System.out.println("EXPRESSION GRAMMAR\nGrammar:\n  E -> E + T\n  E -> T\n  T -> id");

        Grammar grammar = new Grammar("E", Arrays.asList(
                new Grammar.Production("E", new String[]{"E", "+", "T"}),
                new Grammar.Production("E", new String[]{"T"}),
                new Grammar.Production("T", new String[]{"id"})
        ));

        System.out.println("\n--- Function 1: LALR Table Construction ---");
        LR1Builder.BuildResult result = new LR1Builder(grammar).build();
        LALRConstructor constructor = new LALRConstructor(result.canonicalStates, result.canonicalTable);
        ParsingTable lalrTable = constructor.constructLALRTable();
        constructor.reportConflicts();
        lalrTable.printTable();

        System.out.println("\n--- Function 2: LALR Parsing ---");
        LALRParser parser = new LALRParser(lalrTable, result.rules);
        List<TestResult> results = new ArrayList<>();

        results.add(runTest(parser,  1, "id + id",           true));
        results.add(runTest(parser,  2, "id",                true));
        results.add(runTest(parser,  3, "+ id",              false));
        results.add(runTest(parser,  4, "id + id + id",      true));
        results.add(runTest(parser,  5, "id + id + id + id", true));
        results.add(runTest(parser,  6, "id +",              false));
        results.add(runTest(parser,  7, "id id",             false));
        results.add(runTest(parser,  8, "+",                 false));
        results.add(runTest(parser,  9, "id + + id",         false));
        results.add(runTest(parser, 10, "+ +",               false));
        results.add(runTest(parser, 11, "",                  false));
        results.add(runTest(parser, 12, "id + id id",        false));
        results.add(runTest(parser, 13, "id + id +",         false));

        printSummary(results);
    }

    // ── Test runner + summary ─────────────────────────────────────────────────

    static class TestResult {
        final int     num;
        final String  input;
        final boolean expected;
        final boolean actual;

        TestResult(int num, String input, boolean expected, boolean actual) {
            this.num      = num;
            this.input    = input;
            this.expected = expected;
            this.actual   = actual;
        }

        boolean passed() { return expected == actual; }
    }

    static TestResult runTest(LALRParser parser, int num, String input, boolean expectAccept) {
        String label   = expectAccept ? "Valid"   : "Invalid";
        String display = input.isEmpty() ? "(empty)" : "\"" + input + "\"";
        System.out.println("\n--- Test " + num + ": " + label + " Input " + display + " ---");
        boolean actual = parser.parse(input);
        return new TestResult(num, input, expectAccept, actual);
    }

    static void printSummary(List<TestResult> results) {
        int passed = 0;
        for (TestResult r : results) if (r.passed()) passed++;

        System.out.println("\n+-------+----------------------+----------+----------+");
        System.out.println("|                   TEST SUMMARY                     |");
        System.out.println("+-------+----------------------+----------+----------+");
        System.out.println("| Test  | Input                | Expected | Result   |");
        System.out.println("+-------+----------------------+----------+----------+");
        for (TestResult r : results) {
            String display  = r.input.isEmpty() ? "(empty)" : r.input;
            String expected = r.expected ? "accept" : "reject";
            String result   = r.passed() ? "PASS" : "FAIL";
            System.out.printf("| %-5d | %-20s | %-8s | %-8s |%n",
                    r.num, display, expected, result);
        }
        System.out.println("+-------+----------------------+----------+----------+");
        System.out.printf( "| %d / %d passed%-39s|%n", passed, results.size(), "");
        System.out.println("+-------+----------------------+----------+----------+");
    }
}
