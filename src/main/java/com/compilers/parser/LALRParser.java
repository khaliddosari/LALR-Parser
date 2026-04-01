package com.compilers.parser;

import com.compilers.model.ParseResult;
import com.compilers.model.ParseResult.ParseStep;
import com.compilers.model.ParsingTable;

import java.util.ArrayList;
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

    public ParseResult parse(String input) {
        String trimmed = input.trim();
        List<ParseStep> steps = new ArrayList<>();

        Stack<Integer> stack = new Stack<>();
        stack.push(0);

        String[] tokens = trimmed.isEmpty()
                ? new String[]{"$"}
                : (trimmed + " $").split("\\s+");

        int pos   = 0;
        int step  = 0;
        int limit = tokens.length * 20;

        while (true) {
            if (++step > limit) {
                steps.add(new ParseStep(stack.toString(),
                        remaining(tokens, pos),
                        "ERROR: Step limit exceeded"));
                return new ParseResult(false, trimmed, steps, "Step limit exceeded (possible infinite loop)");
            }

            int    state  = stack.peek();
            String symbol = tokens[pos];
            String act    = table.getAction(state, symbol);
            String rem    = remaining(tokens, pos);

            if (act == null) {
                String err = "No action for '" + symbol + "' in state " + state;
                steps.add(new ParseStep(stack.toString(), rem, "ERROR: " + err));
                return new ParseResult(false, trimmed, steps, err);
            }

            steps.add(new ParseStep(stack.toString(), rem, act));

            if (act.equals("accept")) {
                return new ParseResult(true, trimmed, steps, null);
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
                        steps.add(new ParseStep("[]", rem, "ERROR: Stack underflow"));
                        return new ParseResult(false, trimmed, steps, "Stack underflow during reduce");
                    }
                    stack.pop();
                }

                Integer gotoState = table.getGoto(stack.peek(), lhs);
                if (gotoState == null) {
                    String err = "No GOTO for '" + lhs + "' in state " + stack.peek();
                    steps.add(new ParseStep(stack.toString(), rem, "ERROR: " + err));
                    return new ParseResult(false, trimmed, steps, err);
                }
                stack.push(gotoState);
            } else {
                steps.add(new ParseStep(stack.toString(), rem, "ERROR: Unknown action: " + act));
                return new ParseResult(false, trimmed, steps, "Unknown action: " + act);
            }
        }
    }

    private static String remaining(String[] tokens, int pos) {
        return String.join(" ", Arrays.copyOfRange(tokens, pos, tokens.length));
    }
}
