# Story 7.2: Docker Compose for application, database, and LLM

Status: done

<!-- Ultimate context engine analysis completed - comprehensive developer guide created. -->

## Story

As an **operator**,
I want **separate containers for app, Postgres, and LLM**,
so that **we can replace or scale pieces independently in pilot**.

## Acceptance Criteria

1. **Three services** — Given `docker-compose.yml` with **app**, **db**, and **llm** services,
   when `docker compose up` runs with documented env,
   then all three start and app reaches **JDBC** + **AI endpoint** config successfully (**FR32**).

2. **LLM down** — Given LLM container stopped,
   when app health and mentor flows run,
   then behavior matches **Epic 5** degraded expectations (banner + mentor publish) (**NFR8**).

3. **Traceability** — **FR32**, **NFR8**; architecture tri-container topology.

## Tasks / Subtasks

- [x] `docker-compose.yml`: Postgres image (major pinned per architecture); app `build:` or image; LLM (e.g. Ollama) service.
- [x] Wire env: `SPRING_DATASOURCE_*`, Spring AI base URL, **Git** secrets from env (**3.1**).
- [x] App **Dockerfile** (multi-stage optional) for runnable JAR.
- [x] Document prerequisites: `docker compose` version; ports.
- [x] Smoke: app Actuator health + DB reachable from app container.

## Dev Notes

### Prerequisites

- Runnable JAR from Maven; **1.1** Actuator health.

### Architecture compliance

- **FR32** split topology [Source: architecture **Infrastructure & Deployment**].
- `.env` not committed; **7.3** `.env.example`.

### References

- [Source: `_bmad-output/planning-artifacts/epics.md` — Story 7.2]

## Dev Agent Record

### Agent Model Used

Composer (Cursor agent)

### Debug Log References

- `docker-compose config` validated interpolation (including nested defaults for JDBC credentials).
- `./mvnw verify` was started in agent environment; Java/Testcontainers availability is host-dependent.

### Completion Notes List

- Added tri-service **`docker-compose.yml`**: **`db`** (`postgres:16-alpine`), **`llm`** (Ollama image pin + health), **`app`** (image built from **`Dockerfile`**). App defaults: **`SPRING_PROFILES_ACTIVE=dev`**, JDBC to **`db`**, **`OLLAMA_BASE_URL=http://llm:11434`**, **`OLLAMA_MODEL=deepseek-r1:8b`**, **`GIT_PROVIDER_*`** from env; Actuator healthcheck via **`curl`** in the app image.
- **`llm`**: image **`${OLLAMA_IMAGE:-ollama/ollama:0.20.2}`** (override via **`.env`**); **healthcheck** `ollama list`; **`app`** uses **`depends_on` → `service_healthy`** on **`llm`** so the daemon accepts RPC before the JVM starts.
- **`Dockerfile`**: multi-stage Maven wrapper build (JDK 21 Alpine) → JRE 21 Alpine runnable JAR; non-root **`spring`** user; **`curl`** for health checks.
- **`.dockerignore`** to keep build context small; **`.env.example`** documents **`OLLAMA_IMAGE`**, ports, JDBC, **`OLLAMA_MODEL`**; **README** and **`docs/runbook-pilot.md`** document Compose and **`ollama pull deepseek-r1:8b`**.
- **`application-dev.yml`**: Ollama base URL respects **`OLLAMA_BASE_URL`** for the **`llm`** service host.
- **`application.yml`**: default chat model **`OLLAMA_MODEL`** → **`deepseek-r1:8b`** (still overridable by env).

### File List

- `docker-compose.yml`
- `Dockerfile`
- `.dockerignore`
- `.env.example`
- `README.md`
- `docs/runbook-pilot.md`
- `src/main/resources/application-dev.yml`
- `src/main/resources/application.yml`

## Change Log

- **2026-04-08** — Follow-up: Compose Ollama pin + **`OLLAMA_IMAGE`**, **`llm` healthcheck**, **`app` → `llm` `service_healthy`**; default model **`deepseek-r1:8b`** in Compose and **`application.yml`**; docs and **`.env.example`** aligned.

---

**Story completion status:** `done` — completed.
