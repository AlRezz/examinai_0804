# Story 7.2: Docker Compose for application, database, and LLM

Status: ready-for-dev

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

- [ ] `docker-compose.yml`: Postgres image (major pinned per architecture); app `build:` or image; LLM (e.g. Ollama) service.
- [ ] Wire env: `SPRING_DATASOURCE_*`, Spring AI base URL, **Git** secrets from env (**3.1**).
- [ ] App **Dockerfile** (multi-stage optional) for runnable JAR.
- [ ] Document prerequisites: `docker compose` version; ports.
- [ ] Smoke: app Actuator health + DB reachable from app container.

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
_(filled by dev agent)_
### Debug Log References
### Completion Notes List
### File List
_(filled by dev agent on completion)_

---

**Story completion status:** `ready-for-dev` — Ultimate context engine analysis completed.
