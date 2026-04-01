# LALR Parser

A Java implementation of an LALR (Look-Ahead LR) parser, including LALR table construction and stack-based parsing with step-by-step trace output.

## Project Structure

```
.
├── src/main/
│   ├── java/com/compilers/
│   │   ├── Main.java                    # Spring Boot entry point + CLI test runner
│   │   ├── controller/
│   │   │   └── ParseController.java     # REST API endpoint
│   │   ├── model/
│   │   │   ├── Grammar.java             # CFG representation with auto-augmentation
│   │   │   ├── LRItem.java              # LR item with core-only equality for LALR merging
│   │   │   ├── LRState.java             # Set of LR items; supports core merging
│   │   │   └── ParsingTable.java        # ACTION and GOTO tables with conflict detection
│   │   └── parser/
│   │       ├── LR1Builder.java          # Canonical LR(1) construction from grammar
│   │       ├── LALRConstructor.java     # Merges LR(1) states into LALR table
│   │       └── LALRParser.java          # Stack-based LALR parser with trace
│   └── resources/
│       └── static/                      # Web frontend (HTML/JS/CSS)
├── pom.xml
├── Dockerfile
├── docker-compose.yml
├── REPORT.md
└── README.md
```

## Building and Running

### With Maven Wrapper (recommended)
```bash
./mvnw clean package
java -jar target/lalr-parser-1.0.0.jar
```

### Manual (no build tool)
```bash
javac -d bin \
  src/main/java/com/compilers/model/Grammar.java \
  src/main/java/com/compilers/model/LRItem.java \
  src/main/java/com/compilers/model/LRState.java \
  src/main/java/com/compilers/model/ParsingTable.java \
  src/main/java/com/compilers/parser/LR1Builder.java \
  src/main/java/com/compilers/parser/LALRConstructor.java \
  src/main/java/com/compilers/parser/LALRParser.java \
  src/main/java/com/compilers/Main.java
java -cp bin com.compilers.Main
```

### With Docker
```bash
docker compose up --build
```
Then open http://localhost:8080 in your browser.

## Grammars Tested

| Grammar | Productions |
|---------|-------------|
| Simple | `S -> A a \| b`, `A -> c` |
| Expression | `E -> E + T \| T`, `T -> id` |

Each grammar runs 13 test cases covering valid inputs, invalid inputs, empty strings, and edge cases.

## REST API

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/parse` | POST | Parse input against a grammar, returns structured result as JSON |
| `/api/health` | GET | Health check |

### Example request
```json
POST /api/parse
{
  "startSymbol": "E",
  "productions": ["E -> E + T", "E -> T", "T -> id"],
  "input": "id + id"
}
```
