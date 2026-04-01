package com.compilers.controller;

import com.compilers.model.LALRResult;
import com.compilers.model.ParseResult;

public class ParseResponse {
    private LALRResult lalrResult;
    private ParseResult parseResult;

    public ParseResponse(LALRResult lalrResult, ParseResult parseResult) {
        this.lalrResult  = lalrResult;
        this.parseResult = parseResult;
    }

    public LALRResult getLalrResult()    { return lalrResult; }
    public ParseResult getParseResult()  { return parseResult; }
}
