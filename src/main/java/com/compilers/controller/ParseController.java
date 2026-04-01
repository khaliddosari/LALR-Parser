package com.compilers.controller;

import com.compilers.model.Grammar;
import com.compilers.model.LALRResult;
import com.compilers.model.ParseResult;
import com.compilers.model.ParsingTable;
import com.compilers.parser.LALRConstructor;
import com.compilers.parser.LALRParser;
import com.compilers.parser.LR1Builder;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ParseController {

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }

    @PostMapping("/parse")
    public ResponseEntity<?> parse(@RequestBody ParseRequest request) {
        if (request.getStartSymbol() == null || request.getStartSymbol().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "startSymbol is required"));
        }
        if (request.getProductions() == null || request.getProductions().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "productions list is required"));
        }
        if (request.getInput() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "input is required (use empty string for empty input)"));
        }

        try {
            List<Grammar.Production> prods = parseProductions(request.getProductions());
            Grammar grammar = new Grammar(request.getStartSymbol(), prods);

            LR1Builder.BuildResult buildResult = new LR1Builder(grammar).build();
            LALRConstructor constructor = new LALRConstructor(buildResult.canonicalStates, buildResult.canonicalTable);
            LALRResult lalrResult = constructor.buildResult();
            ParsingTable lalrTable = constructor.getLalrTable();

            LALRParser parser = new LALRParser(lalrTable, buildResult.rules);
            ParseResult parseResult = parser.parse(request.getInput());

            return ResponseEntity.ok(new ParseResponse(lalrResult, parseResult));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Parses production strings like "E -> E + T" into Grammar.Production objects.
     */
    private List<Grammar.Production> parseProductions(List<String> raw) {
        List<Grammar.Production> prods = new ArrayList<>();
        for (String line : raw) {
            String[] sides = line.split("->");
            if (sides.length != 2) {
                throw new IllegalArgumentException("Invalid production format: '" + line + "'. Expected 'LHS -> RHS'");
            }
            String lhs = sides[0].trim();
            String rhs = sides[1].trim();
            String[] symbols = rhs.isEmpty() ? new String[0] : rhs.split("\\s+");
            prods.add(new Grammar.Production(lhs, symbols));
        }
        return prods;
    }
}
