# Examinai

## Documentation

- **Product and Sprint traceability:** [`docs/README.md`](docs/README.md) — PRD, architecture, epics, `sprint-status.yaml`, story files, runbook, and how `docs/` relates to `_bmad-output/`.
- **Pilot operations:** [`docs/runbook-pilot.md`](docs/runbook-pilot.md) — health checks, degraded LLM, Git diagnostics, smoke path.

---

Spring Boot baseline for the Examinai product: Java **21**, **Maven**, Thymeleaf, JPA, PostgreSQL driver, Liquibase, Spring Security, Validation, Actuator, and **Spring AI Ollama**. The web UI uses **Bootstrap 5** from **WebJars** (shared Thymeleaf fragment `fragments/head-bootstrap` — story **8.1**) so styles do not depend on a CDN.

## Prerequisites

- JDK 21 or newer
- Maven (or use the included **`./mvnw`** wrapper)
- **PostgreSQL** for the **`dev`** profile (local or container)
- **Docker Compose v2** (optional) for the tri-service pilot stack: app + Postgres + Ollama (**story 7.2**, **FR32**)

Full stack and sequencing are described in [`_bmad-output/planning-artifacts/architecture.md`](_bmad-output/planning-artifacts/architecture.md).

## Configuration (secrets)

Use **environment variables** for credentials—do not commit secrets. See **`.env.example`** for key names (`SPRING_DATASOURCE_*`, optional `DATABASE_*` aliases in **`prod`**, and **`GIT_PROVIDER_*`** for Git integration).

Git HTTP uses **Spring `RestClient` only** (see `com.examinai.app.integration.git.GitSourceClient`). Configure **`GIT_PROVIDER_BASE_URL`** to a GitHub REST v3–compatible API root (e.g. `https://api.github.com`) and **`GIT_PROVIDER_TOKEN`**; requests use **`Accept: application/vnd.github+json`** and **`Authorization: Bearer`** with the configured token when set.

**Fetch behavior (normalized text for review / AI):**

1. **Primary:** [`GET /repos/{owner}/{repo}/commits/{ref}`](https://docs.github.com/en/rest/commits/commits?apiVersion=2026-03-10) — `ref` is the intern’s commit SHA, branch, or tag; response includes commit metadata and a **`files`** array.
2. **Resolve the path scope** (required; there is **no** default file such as `README.md`): for the matching **`files[]`** entry, use in order: **`patch`** (unified diff), else HTTP **`raw_url`**, else **`contents_url`** (Contents API JSON with base64 `content`), else **`GET /repos/{owner}/{repo}/contents/{path}?ref={ref}`** [repository contents](https://docs.github.com/en/rest/repos/contents?apiVersion=2026-03-10).
3. If there is **no** matching row in **`files`** for the requested path, the client **still** tries **repository contents** for `{path}` at `{ref}` so unchanged paths can load when the API allows.

If fetch fails, verify **`owner/repo`**, **ref**, **token** scopes, and **path scope**. Mentor UI maps errors to safe messages (**FR11**); see **`docs/runbook-pilot.md`** for triage.

## AI draft assessments (pilot; FR18 / NFR4 / NFR7)

Mentors can request an **assistive** draft from **Spring AI** (Ollama-backed `ChatClient` in code). Controllers delegate to **`com.examinai.app.integration.ai`** only—no ad-hoc HTTP calls to an LLM from the web layer.

**Data minimization:** The prompt includes the **task title and instructions** plus **truncated normalized source** already stored on the submission after a successful fetch. It does **not** include Git provider tokens, `.env` or credential material, or other deployment secrets.

Configure inference with **`OLLAMA_BASE_URL`**, **`OLLAMA_MODEL`**, and optional bounds: **`EXAMINAI_AI_DRAFT_MAX_SOURCE_CHARS`**, **`EXAMINAI_AI_DRAFT_TIMEOUT_SECONDS`** (≥ 1), **`EXAMINAI_AI_DRAFT_MAX_RETRIES`**, **`EXAMINAI_AI_DRAFT_RETRY_BACKOFF_MS`**, **`EXAMINAI_AI_DRAFT_MAX_INFERENCE_WALL_SECONDS`** (hard cap on all attempts combined), **`EXAMINAI_AI_DRAFT_MAX_FLASH_CHARS`** (flash display until drafts are persisted; see `application.yml`).

**If AI draft fails with HTTP 404 and a body like `model '…' not found`:** Ollama only runs models you have pulled. Default **`OLLAMA_MODEL`** is **`deepseek-r1:8b`**. On the machine where Ollama listens (**`OLLAMA_BASE_URL`**, e.g. `http://127.0.0.1:11434`), run **`ollama pull deepseek-r1:8b`**, or set **`OLLAMA_MODEL`** to a tag you already have (check with **`ollama list`**). With Compose: **`docker compose exec llm ollama pull "$OLLAMA_MODEL"`** (or the concrete tag). The app is configured to **pull the chat model on startup when missing** (`spring.ai.ollama.init.pull-model-strategy`); the first pull can take several minutes. To disable auto-pull (e.g. pre-baked images in production), set **`SPRING_AI_OLLAMA_INIT_PULL_MODEL_STRATEGY=never`**.

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

**Homebrew `docker` on macOS:** if the build fails with **`docker-credential-desktop` … not found in `$PATH`**, your CLI is probably from Homebrew while credentials are handled by Docker Desktop. Either add Docker Desktop’s tools to your `PATH` (`export PATH="/Applications/Docker.app/Contents/Resources/bin:$PATH"`) or use the repo helper (adds that `PATH` and invokes Homebrew’s **`docker`** so **`docker compose`** still loads the Compose v2 plugin):

```bash
./scripts/docker-with-desktop-path.sh compose up -d --build
```

See also **[`docs/runbook-pilot.md`](docs/runbook-pilot.md)** (*docker-credential-desktop* troubleshooting).

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

## User flows (by role)

The app distinguishes four roles (database values): **`intern`**, **`mentor`**, **`administrator`**, **`coordinator`**. After sign-in, **`/home`** lists the navigation links your role may use.

**Who creates tasks and assignments:** **`/tasks`** (create/edit tasks, assign interns, open submissions) is available to **mentors and administrators** (`SecurityConfig`: **`MENTOR`** and **`ADMINISTRATOR`** on **`/tasks/**`**). **User accounts** (creating **`intern`** / **`mentor`** logins) remain under **`/admin/users`**, **administrator-only**.

**Git and AI (optional):** For **Fetch source**, set **`GIT_PROVIDER_*`** and ensure coordinates include a **non-empty path scope** (see Configuration above). For **Generate AI draft**, the LLM must be reachable (e.g. Compose **`llm`** service); if inference fails, the UI supports **mentor-only** scoring and publish (**NFR8**).

### Suggested end-to-end order

1. **Administrator** creates **intern** (and optionally **mentor**) accounts under **`/admin/users`** when needed.  
2. **Mentor or administrator** creates a **task** (**`/tasks/new`**) and **assigns interns** (**`/tasks/{task-id}/assignments`**).  
3. **Intern** opens the task and **submits** version-control **coordinates** (repository, commit ref, **path scope** — required for fetch).  
4. **Mentor or administrator** opens the submission from the **review queue** or **task submissions**, **fetches** source, optionally generates an **AI draft** (shown read-only), then completes the **rubric** and **publishes** the official review.

---

### Administrator — user accounts only

1. Sign in at **`/login`** (e.g. bootstrap **`admin@examinai.local`**).  
2. **User management** — **`/admin/users`**: **Create user** (**`/admin/users/new`**) adds accounts; assign roles (**`intern`**, **`mentor`**, **`coordinator`**, **`administrator`**, etc.).  
3. Mentors cannot access **`/admin/users`**; they rely on an administrator for **new logins** (unless accounts already exist).

---

### Mentor or administrator — create a task and assign interns

1. Sign in as **`mentor`** or **`administrator`**. Mentors land on **`/tasks`** after login; administrators may use **`/home`** → **Program tasks**.  
2. **Program tasks** — **`/tasks`**: **New task** (**`/tasks/new`**): title, description, due date — submit.  
3. On the task list: **Assign interns** (**`/tasks/{task-id}/assignments`**). Check one or more users that already have the **`intern`** role (the form **replaces** all assignments for that task) — **Save**.  
4. **Submissions** (**`/tasks/{task-id}/submissions`**) lists assigned interns; each row links to the mentor **submission detail** workspace.

---

### Intern — submit coordinates for review

1. Sign in as a user with the **`intern`** role.  
2. Open **My assigned tasks** — **`/intern/tasks`**.  
3. Open a task — **`/intern/tasks/{task-id}`**.  
4. Enter **repository identifier**, **commit SHA**, and **path scope** (file path within the repo for fetch), then submit the form. The app stores or updates the **submission** for that intern and task and marks it ready for mentor review workflows.

---

### Mentor or administrator — move work through review and publish

1. Sign in as **`mentor`** or **`administrator`** (both can use the review queue and submission pages).  
2. Open the **Review queue** — **`/review/queue`** — and follow a submission link, **or** go to **Program tasks** (**`/tasks`**) → **Submissions** for a task — **`/tasks/{task-id}/submissions`** — then open an intern’s row (**`/tasks/{task-id}/submissions/{intern-id}`**).  
3. On the **submission detail** page, if coordinates are missing or incorrect, fill the form and use **Save coordinates** so the submission exists in **submitted** state.  
4. Choose **Fetch source** to pull normalized text from the Git provider (requires successful Git configuration and valid coordinates).  
5. **Generate AI draft** — the app calls the LLM with the **fetched** submission text (requires successful fetch and a working model). The model response must include **feedback on the code** and **suggestions to improve**; this assistive draft is **not** the official outcome.  
6. The submission detail page shows the AI draft in **read-only** fields for the mentor (you cannot edit the model text there).  
7. Enter **quality**, **readability**, and **correctness** scores (1–5) and **mentor feedback** text. Use **Save draft** to store work in progress, or **Publish official review** to record the **published** outcome for the intern.

---

### Coordinator — read-only audit

1. Sign in as a user with the **`coordinator`** role.  
2. The coordinator landing page — **`/coordinator`** — explains the URL pattern.  
3. Open a read-only case record at **`/coordinator/cases/{submission-id}`**, replacing **`{submission-id}`** with the submission **UUID** (for example copied from mentor URLs or internal tools).

## Tests

```bash
./mvnw verify
```

The **`test`** profile uses **H2** (in PostgreSQL compatibility mode) plus the same Liquibase changelog. Each Spring test context gets its own in-memory database name (`examinai_test_<random>`) so Liquibase always starts from a clean state. **`PostgresLiquibaseIntegrationTest`** uses **Testcontainers** against PostgreSQL when **Docker** is available; it is **skipped** otherwise so `mvn verify` still passes.

## Spring Boot version note

The **Spring Initializr** POM may list the parent as `3.5.13.RELEASE`; **Maven Central** publishes **`3.5.13`** for the same line. This project uses **`3.5.13`** as the parent version so the build resolves. Re-check [start.spring.io](https://start.spring.io/) and [Spring AI getting started](https://docs.spring.io/spring-ai/reference/getting-started.html) when upgrading.
