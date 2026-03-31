package com.compilers.parser;

import com.compilers.model.ParsingTable;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class LALRParser {
    final ParsingTable table;
    final List<String> rules;

    public LALRParser(ParsingTable table, List<String> rules) {
        this.table = table;
        this.rules = rules;
    }

    public boolean parse(String input) {
        String trimmed = input.trim();
        System.out.println("\n=== LALR PARSING ===");
        System.out.println("Input: " + (trimmed.isEmpty() ? "(empty)" : trimmed));
        System.out.println("\nStack\t\tInput\t\tAction\n----------------------------------------");

        Stack<Integer> stack = new Stack<>();
        stack.push(0);

        // Split into tokens; handle empty input cleanly
        String[] tokens = trimmed.isEmpty()
                ? new String[]{"$"}
                : (trimmed + " $").split("\\s+");

        int pos   = 0;
        int step  = 0;
        int limit = tokens.length * 20; // O(n) bound: each token causes at most a fixed number of steps

        while (true) {
            if (++step > limit) {
                System.out.println("ERROR: Step limit exceeded (possible infinite loop)");
                return false;
            }

            int    state  = stack.peek();
            String symbol = tokens[pos];
            String act    = table.getAction(state, symbol);

            System.out.printf("%-15s\t%-15s\t",
                    stack.toString(),
                    String.join(" ", Arrays.copyOfRange(tokens, pos, tokens.length)));

            if (act == null) {
                System.out.println("ERROR: No action for '" + symbol + "' in state " + state);
                return false;
            }
            System.out.println(act);

            if (act.equals("accept")) {
                System.out.println("\n✓ String accepted!");
                return true;
            } else if (act.startsWith("s")) {
                stack.push(Integer.parseInt(act.substring(1)));
                pos++;
            } else if (act.startsWith("r")) {
                String rule    = rules.get(Integer.parseInt(act.substring(1)));
                String[] parts = rule.split("->");
                String lhs     = parts[0].trim();
                String rhs     = parts.length > 1 ? parts[1].trim() : "";
                int popCount   = rhs.isEmpty() ? 0 : rhs.split("\\s+").length;

                for (int i = 0; i < popCount; i++) {
                    if (stack.isEmpty()) {
                        System.out.println("ERROR: Stack underflow during reduce");
                        return false;
                    }
                    stack.pop();
                }

                Integer gotoState = table.getGoto(stack.peek(), lhs);
                if (gotoState == null) {
                    System.out.println("ERROR: No GOTO for '" + lhs + "' in state " + stack.peek());
                    return false;
                }
                stack.push(gotoState);
            } else {
                System.out.println("ERROR: Unknown action: " + act);
                return false;
            }
        }
    }
}
