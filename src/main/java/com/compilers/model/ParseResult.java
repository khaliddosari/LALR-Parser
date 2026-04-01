package com.compilers.model;

import java.util.List;

public class ParseResult {
    private final boolean accepted;
    private final String input;
    private final List<ParseStep> steps;
    private final String error;

    public ParseResult(boolean accepted, String input, List<ParseStep> steps, String error) {
        this.accepted = accepted;
        this.input    = input;
        this.steps    = steps;
        this.error    = error;
    }

    public boolean isAccepted()     { return accepted; }
    public String getInput()        { return input; }
    public List<ParseStep> getSteps() { return steps; }
    public String getError()        { return error; }

    public static class ParseStep {
        private final String stack;
        private final String remainingInput;
        private final String action;

        public ParseStep(String stack, String remainingInput, String action) {
            this.stack          = stack;
            this.remainingInput = remainingInput;
            this.action         = action;
        }

        public String getStack()          { return stack; }
        public String getRemainingInput() { return remainingInput; }
        public String getAction()         { return action; }
    }
}
