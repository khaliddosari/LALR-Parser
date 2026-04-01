package com.compilers.controller;

import java.util.List;

public class ParseRequest {
    private String startSymbol;
    private List<String> productions;
    private String input;

    public String getStartSymbol()              { return startSymbol; }
    public void setStartSymbol(String s)        { this.startSymbol = s; }
    public List<String> getProductions()         { return productions; }
    public void setProductions(List<String> p)   { this.productions = p; }
    public String getInput()                     { return input; }
    public void setInput(String i)               { this.input = i; }
}
