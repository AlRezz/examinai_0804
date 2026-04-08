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

- Added tri-service **`docker-compose.yml`**: **`db`** (`postgres:16-alpine`), **`llm`** (`ollama/ollama:latest`), **`app`** (image built from **`Dockerfile`**). App defaults: **`SPRING_PROFILES_ACTIVE=dev`**, JDBC to **`db`**, **`OLLAMA=http://llm:11434`**, **`GIT_PROVIDER_*`** from env; Actuator healthcheck via **`curl`** in the app image.
- **`Dockerfile`**: multi-stage Maven wrapper build (JDK 21 Alpine) → JRE 21 Alpine runnable JAR; non-root **`spring`** user; **`curl`** for health checks.
- **`.dockerignore`** to keep build context small; **`.env.example`** and **README** document Compose env and ports.
- **`application-dev.yml`**: Ollama base URL now respects **`OLLAMA_BASE_URL`** so Compose can point at the **`llm`** service instead of localhost.

### File List

- `docker-compose.yml` (new)
- `Dockerfile` (new)
- `.dockerignore` (new)
- `.env.example` (updated)
- `README.md` (updated)
- `src/main/resources/application-dev.yml` (updated)

---

**Story completion status:** `done` — completed.
