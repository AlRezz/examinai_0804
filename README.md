# Examinai

Spring Boot baseline for the Examinai product: Java **21**, **Maven**, Thymeleaf, JPA, PostgreSQL driver, Liquibase, Spring Security, Validation, Actuator, and **Spring AI Ollama**.

## Prerequisites

- JDK 21 or newer
- Maven (or use the included **`./mvnw`** wrapper)
- **PostgreSQL** for the **`dev`** profile (local or container)

Full stack and sequencing are described in [`_bmad-output/planning-artifacts/architecture.md`](_bmad-output/planning-artifacts/architecture.md).

## Configuration (secrets)

Use **environment variables** for credentials—do not commit secrets. See **`.env.example`** for key names (`SPRING_DATASOURCE_*`, and optional `DATABASE_*` aliases in **`prod`**).

Example local database with Docker:

```bash
docker run -d --name examinai-pg \
  -e POSTGRES_USER=examinai \
  -e POSTGRES_PASSWORD=examinai \
  -e POSTGRES_DB=examinai \
  -p 5432:5432 \
  postgres:16-alpine
```

## Run locally (dev profile)

The **`dev`** profile expects PostgreSQL at **`jdbc:postgresql://localhost:5432/examinai`** with user **`examinai`** and password **`examinai`** by default. Override with **`SPRING_DATASOURCE_URL`**, **`SPRING_DATASOURCE_USERNAME`**, and **`SPRING_DATASOURCE_PASSWORD`**.

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Confirm Actuator health (default path):

```bash
curl -sSf http://localhost:8080/actuator/health
```

## Production-oriented profile

Smoke-style run (set **`SPRING_DATASOURCE_*`** or **`DATABASE_URL`** / **`DATABASE_USERNAME`** / **`DATABASE_PASSWORD`** and optional **`OLLAMA_BASE_URL`**):

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

## Local pilot admin (story 1.3)

After migrations run, a **bootstrap administrator** exists for development and automated tests only:

- **Email:** `admin@examinai.local`
- **Temporary password:** `ChangeMe!Dev1` (documented here only; **not** stored as plaintext in the database or migrations)

The row stores a **BCrypt** hash. **Change this password** (or drop the user) before any shared or production-like environment. With the app running, open **`/login`** and sign in with the email and password above; administrators are redirected to **`/admin/users`** for account management (stories **1.4–1.5**).

## Tests

```bash
./mvnw verify
```

The **`test`** profile uses **H2** (in PostgreSQL compatibility mode) plus the same Liquibase changelog. Each Spring test context gets its own in-memory database name (`examinai_test_<random>`) so Liquibase always starts from a clean state. **`PostgresLiquibaseIntegrationTest`** uses **Testcontainers** against PostgreSQL when **Docker** is available; it is **skipped** otherwise so `mvn verify` still passes.

## Spring Boot version note

The **Spring Initializr** POM may list the parent as `3.5.13.RELEASE`; **Maven Central** publishes **`3.5.13`** for the same line. This project uses **`3.5.13`** as the parent version so the build resolves. Re-check [start.spring.io](https://start.spring.io/) and [Spring AI getting started](https://docs.spring.io/spring-ai/reference/getting-started.html) when upgrading.
