# LALR Parser

A Java implementation of an LALR (Look-Ahead LR) parser, including LALR table construction and stack-based parsing with step-by-step trace output.

## Project Structure

```
.
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── compilers/
│                   ├── Main.java              # Entry point and test cases
│                   ├── model/
│                   │   ├── LRItem.java        # LR item: lhs -> rhs with dot and lookahead
│                   │   ├── LRState.java       # Set of LR items; supports core merging
│                   │   └── ParsingTable.java  # ACTION and GOTO tables with conflict detection
│                   └── parser/
│                       ├── LALRConstructor.java  # Builds LALR table from canonical LR states
│                       └── LALRParser.java       # Stack-based LALR parser with trace output
├── bin/                   # Compiled .class files
├── REPORT.md
└── README.md
```

## Building and Running

### Compile
```bash
javac -d bin \
  src/main/java/com/compilers/model/LRItem.java \
  src/main/java/com/compilers/model/LRState.java \
  src/main/java/com/compilers/model/ParsingTable.java \
  src/main/java/com/compilers/parser/LALRConstructor.java \
  src/main/java/com/compilers/parser/LALRParser.java \
  src/main/java/com/compilers/Main.java
```

### Run
```bash
java -cp bin com.compilers.Main
```

## Grammars Tested

| Grammar | Productions |
|---------|-------------|
| Simple | `S -> A a \| b`, `A -> c` |
| Expression | `E -> E + T \| T`, `T -> id` |

Each grammar runs 13 test cases covering valid inputs, invalid inputs, empty strings, and edge cases.

## Planned: Spring Boot Web Interface

The project will be extended into a web application using Spring Boot and Maven. The model and parser packages will remain unchanged — a REST controller will wrap the parser and serve results to a browser frontend.
