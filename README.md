# Examinai

Spring Boot baseline for the Examinai product: Java **21**, **Maven**, Thymeleaf, JPA, PostgreSQL driver, Liquibase, Spring Security, Validation, Actuator, and **Spring AI Ollama**.

## Prerequisites

- JDK 21 or newer
- Maven (or use the included **`./mvnw`** wrapper)

Full stack and sequencing are described in [`_bmad-output/planning-artifacts/architecture.md`](_bmad-output/planning-artifacts/architecture.md).

## Run locally (dev profile)

Development uses an **in-memory H2** database so the app starts without PostgreSQL. Story 1.2 will wire real Postgres and migrations for shared environments.

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Confirm Actuator health (default path):

```bash
curl -sSf http://localhost:8080/actuator/health
```

## Production-oriented profile

Smoke-style run (expects **`DATABASE_URL`** / **`DATABASE_USERNAME`** / **`DATABASE_PASSWORD`** and optional **`OLLAMA_BASE_URL`**):

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

## Tests

```bash
./mvnw verify
```

The **`test`** profile uses H2 and Liquibase; it does not require Docker or PostgreSQL.

## Spring Boot version note

The **Spring Initializr** POM may list the parent as `3.5.13.RELEASE`; **Maven Central** publishes **`3.5.13`** for the same line. This project uses **`3.5.13`** as the parent version so the build resolves. Re-check [start.spring.io](https://start.spring.io/) and [Spring AI getting started](https://docs.spring.io/spring-ai/reference/getting-started.html) when upgrading.
