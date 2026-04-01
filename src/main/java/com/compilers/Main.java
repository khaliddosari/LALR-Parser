package com.compilers;

import com.compilers.model.Grammar;
import com.compilers.model.LALRResult;
import com.compilers.model.ParseResult;
import com.compilers.model.ParseResult.ParseStep;
import com.compilers.model.ParsingTable;
import com.compilers.parser.LALRConstructor;
import com.compilers.parser.LALRParser;
import com.compilers.parser.LR1Builder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("--cli")) {
            runCliTests();
        } else {
            SpringApplication.run(Main.class, args);
        }
    }

    static void runCliTests() {
        System.out.println("========================================");
        System.out.println("LALR PARSER IMPLEMENTATION");
        System.out.println("========================================\n");
        testSimpleGrammar();
        System.out.println("\n\n========================================\n\n");
        testExpressionGrammar();
    }

    static void testSimpleGrammar() {
        System.out.println("SIMPLE GRAMMAR\nGrammar:\n  S -> A a\n  S -> b\n  A -> c");

        Grammar grammar = new Grammar("S", Arrays.asList(
                new Grammar.Production("S", new String[]{"A", "a"}),
                new Grammar.Production("S", new String[]{"b"}),
                new Grammar.Production("A", new String[]{"c"})
        ));

        LR1Builder.BuildResult buildResult = new LR1Builder(grammar).build();
        LALRConstructor constructor = new LALRConstructor(buildResult.canonicalStates, buildResult.canonicalTable);
        LALRResult lalrResult = constructor.buildResult();
        ParsingTable lalrTable = constructor.getLalrTable();

        printLALRResult(lalrResult);

        LALRParser parser = new LALRParser(lalrTable, buildResult.rules);
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

    static void testExpressionGrammar() {
        System.out.println("EXPRESSION GRAMMAR\nGrammar:\n  E -> E + T\n  E -> T\n  T -> id");

        Grammar grammar = new Grammar("E", Arrays.asList(
                new Grammar.Production("E", new String[]{"E", "+", "T"}),
                new Grammar.Production("E", new String[]{"T"}),
                new Grammar.Production("T", new String[]{"id"})
        ));

        LR1Builder.BuildResult buildResult = new LR1Builder(grammar).build();
        LALRConstructor constructor = new LALRConstructor(buildResult.canonicalStates, buildResult.canonicalTable);
        LALRResult lalrResult = constructor.buildResult();
        ParsingTable lalrTable = constructor.getLalrTable();

        printLALRResult(lalrResult);

        LALRParser parser = new LALRParser(lalrTable, buildResult.rules);
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

    // --- CLI printing helpers ---

    static void printLALRResult(LALRResult r) {
        System.out.println("\n--- LALR Table Construction ---");
        System.out.println("Canonical states: " + r.getCanonicalStateCount());
        System.out.println("LALR states:      " + r.getLalrStateCount());
        for (String msg : r.getMergeLog()) System.out.println("  " + msg);

        if (r.isLALR()) {
            System.out.println("\nNo conflicts found. Grammar is LALR.");
        } else {
            System.out.println("\nConflicts found:");
            for (String c : r.getConflicts()) System.out.println("  - " + c);
        }

        LALRResult.TableData td = r.getTableData();
        System.out.println("\nACTION TABLE:");
        System.out.print("State\t");
        for (String t : td.getTerminals()) System.out.print(t + "\t");
        System.out.println();
        int maxState = td.getActionTable().keySet().stream().mapToInt(i -> i).max().orElse(-1);
        for (int s = 0; s <= maxState; s++) {
            System.out.print(s + "\t");
            var row = td.getActionTable().getOrDefault(s, java.util.Collections.emptyMap());
            for (String t : td.getTerminals())
                System.out.print(row.getOrDefault(t, "") + "\t");
            System.out.println();
        }

        System.out.println("\nGOTO TABLE:");
        System.out.print("State\t");
        for (String nt : td.getNonterminals()) System.out.print(nt + "\t");
        System.out.println();
        int maxGoto = td.getGotoTable().keySet().stream().mapToInt(i -> i).max().orElse(-1);
        for (int s = 0; s <= Math.max(maxState, maxGoto); s++) {
            System.out.print(s + "\t");
            var row = td.getGotoTable().getOrDefault(s, java.util.Collections.emptyMap());
            for (String nt : td.getNonterminals()) {
                Integer g = row.get(nt);
                System.out.print((g != null ? g : "") + "\t");
            }
            System.out.println();
        }
    }

    static void printParseResult(ParseResult pr) {
        String display = pr.getInput().isEmpty() ? "(empty)" : pr.getInput();
        System.out.println("\n=== LALR PARSING ===");
        System.out.println("Input: " + display);
        System.out.println("\nStack\t\tInput\t\tAction\n----------------------------------------");
        for (ParseStep step : pr.getSteps()) {
            System.out.printf("%-15s\t%-15s\t%s%n",
                    step.getStack(), step.getRemainingInput(), step.getAction());
        }
        System.out.println(pr.isAccepted() ? "\nString accepted!" : "\nString rejected.");
    }

    // --- Test runner + summary ---

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
        ParseResult pr = parser.parse(input);
        printParseResult(pr);
        return new TestResult(num, input, expectAccept, pr.isAccepted());
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
