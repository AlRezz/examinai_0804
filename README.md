# Examinai

Spring Boot baseline for the Examinai product: Java **21**, **Maven**, Thymeleaf, JPA, PostgreSQL driver, Liquibase, Spring Security, Validation, Actuator, and **Spring AI Ollama**.

## Prerequisites

- JDK 21 or newer
- Maven (or use the included **`./mvnw`** wrapper)
- **PostgreSQL** for the **`dev`** profile (local or container)
- **Docker Compose v2** (optional) for the tri-service pilot stack: app + Postgres + Ollama (**story 7.2**, **FR32**)

Full stack and sequencing are described in [`_bmad-output/planning-artifacts/architecture.md`](_bmad-output/planning-artifacts/architecture.md).

## Configuration (secrets)

Use **environment variables** for credentials—do not commit secrets. See **`.env.example`** for key names (`SPRING_DATASOURCE_*`, optional `DATABASE_*` aliases in **`prod`**, and **`GIT_PROVIDER_*`** for Git integration).

Git HTTP calls use **Spring `RestClient` only** (not `WebClient`); configure **`GIT_PROVIDER_BASE_URL`** to a GitHub REST v3–compatible API root when ingesting source (e.g. `https://api.github.com`).

## AI draft assessments (pilot; FR18 / NFR4 / NFR7)

Mentors can request an **assistive** draft from **Spring AI** (Ollama-backed `ChatClient` in code). Controllers delegate to **`com.examinai.app.integration.ai`** only—no ad-hoc HTTP calls to an LLM from the web layer.

**Data minimization:** The prompt includes the **task title and instructions** plus **truncated normalized source** already stored on the submission after a successful fetch. It does **not** include Git provider tokens, `.env` or credential material, or other deployment secrets.

Configure inference with **`OLLAMA_BASE_URL`**, **`OLLAMA_MODEL`**, and optional bounds: **`EXAMINAI_AI_DRAFT_MAX_SOURCE_CHARS`**, **`EXAMINAI_AI_DRAFT_TIMEOUT_SECONDS`** (≥ 1), **`EXAMINAI_AI_DRAFT_MAX_RETRIES`**, **`EXAMINAI_AI_DRAFT_RETRY_BACKOFF_MS`**, **`EXAMINAI_AI_DRAFT_MAX_INFERENCE_WALL_SECONDS`** (hard cap on all attempts combined), **`EXAMINAI_AI_DRAFT_MAX_FLASH_CHARS`** (flash display until drafts are persisted; see `application.yml`).

Example local database with Docker:

```bash
docker run -d --name examinai-pg \
  -e POSTGRES_USER=examinai \
  -e POSTGRES_PASSWORD=examinai \
  -e POSTGRES_DB=examinai \
  -p 5432:5432 \
  postgres:16-alpine
```

## Docker Compose (app + database + LLM)

Pilot topology matches **FR32**: separate containers for the Spring Boot app, **PostgreSQL 16**, and **Ollama** so each piece can be replaced or scaled independently.

1. Copy **`.env.example`** to **`.env`** and set **`GIT_PROVIDER_TOKEN`** (and any non-default DB credentials).
2. From the repo root (use **`docker-compose up --build`** if your install only provides the standalone Compose v1 binary):

```bash
docker compose up --build
```

3. Smoke-check Actuator from the host:

```bash
curl -sSf http://localhost:8080/actuator/health
```

The app runs with profile **`dev`** by default under Compose (HTTP sessions without requiring TLS on localhost). **`OLLAMA_BASE_URL`** defaults to **`http://llm:11434`** inside the stack; JDBC targets host **`db`**. If the **LLM** container is stopped, mentor flows follow **Epic 5** degraded behavior (inference banner, mentor-only publish — **NFR8**). The **`llm`** service entrypoint runs **`ollama pull`** for **`OLLAMA_MODEL`** (default **`deepseek-r1:8b`**); the first run may take several minutes. To pull manually or after changing the model: `docker compose exec llm ollama pull deepseek-r1:8b`.

Published ports default to **8080** (app), **5432** (Postgres), **11434** (Ollama); override with **`APP_PUBLISH_PORT`**, **`POSTGRES_PUBLISH_PORT`**, **`OLLAMA_PUBLISH_PORT`** in **`.env`** (see **`.env.example`**).

Operator smoke path, Git-safe diagnostics, and prod Actuator notes: **[`docs/runbook-pilot.md`](docs/runbook-pilot.md)** (story **7.3**).

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
