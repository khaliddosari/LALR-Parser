# LALR Parser

A Java implementation of an LALR(1) parser with a Spring Boot web interface. Input any context-free grammar, get the full parsing table and a step-by-step parse trace.

**Live:** https://lalr-parser.fly.dev

## Run Locally

```bash
./mvnw clean package -DskipTests
java -jar target/lalr-parser-1.0.0.jar
```

Or with Docker:

```bash
docker compose up --build
```

Open http://localhost:8080

## API

```
POST /api/parse
{
  "startSymbol": "E",
  "productions": ["E -> E + T", "E -> T", "T -> id"],
  "input": "id + id"
}
```
