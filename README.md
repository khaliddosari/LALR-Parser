# LALR Parser

A web-based LALR(1) parser that takes any context-free grammar and an input string, builds the full ACTION/GOTO table, and runs the parse with a step-by-step trace showing whether the input is accepted or rejected.

---

## Tech Stack

- **Backend:** `Java 21`, `Spring Boot 3.5`
- **Build:** `Maven`
- **Frontend:** `HTML`, `CSS`, `JavaScript`
- **Libraries:** `AOS scroll animations`
- **Containerization:** `Docker`, `Docker Compose`
- **Deployment:** `Fly.io`

---

## Features

- **`Grammar Input`**: Define any context-free grammar via the web interface using a simple production rule syntax.
- **`LALR(1) Table Construction`**: Builds canonical LR(0) item sets, merges them into LALR states, computes FOLLOW sets, and generates the ACTION/GOTO tables.
- **`Parse Trace`**: Step-by-step table showing stack state, remaining input, and action (shift, reduce, accept, error) for every parsing step.
- **`Accept / Reject Verdict`**: Clear badge output on whether the input string belongs to the grammar's language.
- **`Conflict Detection`**: Detects and reports shift/reduce and reduce/reduce conflicts with a merge log.
- **`Grammar Presets`**: One-click presets for common grammars (simple, expression, arithmetic, parentheses) to get started instantly.
- **`LALR Badge`**: Confirms whether the grammar is valid LALR(1) or not.

---

## Process

1. Implemented the LALR(1) algorithm from scratch in Java: LR(0) item set construction → canonical state merging → FOLLOW set computation → ACTION/GOTO table generation.
2. Built a Spring Boot REST controller to accept grammar + input string as JSON and return the full parse result.
3. Wrote a vanilla JS frontend with AOS animations that sends requests and renders the step-by-step parse trace and tables.
4. Containerized with Docker and deployed to Fly.io.

---

## Running the Project

- ### Locally with Maven

  ```bash
  ./mvnw spring-boot:run
  ```

- ### With Docker

  ```bash
  docker-compose up --build
  ```

  Opens at: `http://localhost:8080` for `Docker` and `Maven`

Live at: [lalr-parser.fly.dev](https://lalr-parser.fly.dev)