# LALR Parser

A Java implementation of an LALR(1) parser with a Spring Boot web interface. Given any context-free grammar, the system constructs the canonical LR(1) item sets, merges same-core states into an LALR parsing table, detects conflicts, and parses input strings with a step-by-step trace.

## Project Structure

```
.
├── src/main/
│   ├── java/com/compilers/
│   │   ├── Main.java                    # Spring Boot entry point
│   │   ├── controller/
│   │   │   ├── ParseController.java     # REST API endpoint
│   │   │   ├── ParseRequest.java        # Request DTO
│   │   │   └── ParseResponse.java       # Response DTO
│   │   ├── model/
│   │   │   ├── Grammar.java             # CFG representation with auto-augmentation
│   │   │   ├── LALRResult.java          # Structured LALR construction result
│   │   │   ├── LRItem.java              # LR item with core-only equality for LALR merging
│   │   │   ├── LRState.java             # Set of LR items; supports core merging
│   │   │   ├── ParseResult.java         # Structured parse result with step trace
│   │   │   └── ParsingTable.java        # ACTION and GOTO tables with conflict detection
│   │   └── parser/
│   │       ├── LR1Builder.java          # Canonical LR(1) construction from grammar
│   │       ├── LALRConstructor.java     # Merges LR(1) states into LALR table
│   │       └── LALRParser.java          # Stack-based LALR parser with trace
│   └── resources/
│       ├── static/                      # Web frontend (HTML/JS/CSS)
│       └── application.properties
├── pom.xml
├── Dockerfile
├── docker-compose.yml
└── README.md
```

## Building and Running

### With Maven Wrapper (recommended)

```bash
./mvnw clean package -DskipTests
java -jar target/lalr-parser-1.0.0.jar
```

Then open http://localhost:8080 in your browser.

> **Windows note:** If `JAVA_HOME` is not set, run this first:
> ```powershell
> $env:JAVA_HOME = "C:\Program Files\Java\jdk-25"
> ```

### With Docker

```bash
docker compose up --build
```

Then open http://localhost:8080 in your browser.

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

## Test Cases

The parser has been validated against 26 test cases across two grammars.

### Simple Grammar

Productions: `S -> A a`, `S -> b`, `A -> c`

| # | Input | Expected | Result |
|---|-------|----------|--------|
| 1 | `c a` | accept | PASS |
| 2 | `b` | accept | PASS |
| 3 | `a b` | reject | PASS |
| 4 | `c` | reject | PASS |
| 5 | `a` | reject | PASS |
| 6 | `c c` | reject | PASS |
| 7 | `b a` | reject | PASS |
| 8 | `c b` | reject | PASS |
| 9 | `a a` | reject | PASS |
| 10 | `b b` | reject | PASS |
| 11 | `c a a` | reject | PASS |
| 12 | *(empty)* | reject | PASS |
| 13 | `b c` | reject | PASS |

### Expression Grammar

Productions: `E -> E + T`, `E -> T`, `T -> id`

| # | Input | Expected | Result |
|---|-------|----------|--------|
| 1 | `id + id` | accept | PASS |
| 2 | `id` | accept | PASS |
| 3 | `+ id` | reject | PASS |
| 4 | `id + id + id` | accept | PASS |
| 5 | `id + id + id + id` | accept | PASS |
| 6 | `id +` | reject | PASS |
| 7 | `id id` | reject | PASS |
| 8 | `+` | reject | PASS |
| 9 | `id + + id` | reject | PASS |
| 10 | `+ +` | reject | PASS |
| 11 | *(empty)* | reject | PASS |
| 12 | `id + id id` | reject | PASS |
| 13 | `id + id +` | reject | PASS |

**26 / 26 passed**
