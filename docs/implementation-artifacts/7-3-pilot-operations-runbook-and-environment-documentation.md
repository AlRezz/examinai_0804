# Story 7.3: Pilot operations runbook and environment documentation

Status: done

<!-- Ultimate context engine analysis completed - comprehensive developer guide created. -->

## Story

As an **operator**,
I want **`.env.example` and a short runbook for smoke tests (login, fetch, optional LLM call)**,
so that **new laptops can join the pilot safely**.

## Acceptance Criteria

1. **`.env.example`** — Lists **keys** and descriptions only; **no** real credentials; matches **7.2** Compose and app config (**NFR12**, architecture).

2. **Runbook** — Documents: health endpoints, typical **Git** failure codes (secret-safe), **degraded LLM** behavior; smoke path: login → retrieval → optional AI.

3. **Prod posture** — **NFR6**: user-facing errors without stack traces; **Actuator** restricted per architecture in prod profile.

4. **Traceability** — Additional requirements runbook; **NFR8**, **NFR12**.

## Tasks / Subtasks

- [x] Add/update `.env.example` at repo root; cross-check **3.1** Git vars, datasource, Spring AI.
- [x] `docs/runbook-pilot.md` (or `RUNBOOK.md`) — short operator-focused.
- [x] Verify `application-prod.yml` (or pilot) hides sensitive Actuator; link to architecture ADR.
- [x] Optional: CI checklist snippet.

## Dev Notes

### Prerequisites

- **7.2** Compose; stable health and degraded paths from **5.4**.

### References

- [Source: `_bmad-output/planning-artifacts/epics.md` — Story 7.3]

## Dev Agent Record

### Agent Model Used

Composer (Cursor agent)

### Debug Log References

### Completion Notes List

- Expanded **`.env.example`** with **`DATABASE_*`** aliases for **`prod`**, optional **`examinai.*` / AI draft** env keys, and NFR12 reminder; aligned with **`docker-compose.yml`** and **`application.yml`**.
- Added **`docs/runbook-pilot.md`**: health table, Git HTTP → **`GitFailureKind`** mapping (secret-safe), degraded LLM (**NFR8**), smoke path through mentor **`…/fetch`**, **`prod`** NFR6 + Actuator vs **`architecture.md`**, CI snippet, NFR8/NFR12 traceability.
- **`application-prod.yml`**: top comment cross-links NFR6 and architecture observability; verified **`management.endpoints.web.exposure.include: health`** only.
- **`README.md`**: link to runbook.

### File List

- `.env.example`
- `docs/runbook-pilot.md`
- `src/main/resources/application-prod.yml`
- `README.md`
- `_bmad-output/implementation-artifacts/7-3-pilot-operations-runbook-and-environment-documentation.md`
- `_bmad-output/implementation-artifacts/sprint-status.yaml`
- `docs/implementation-artifacts/7-3-pilot-operations-runbook-and-environment-documentation.md`

## Change Log

- **2026-04-08** — Story 7.3: pilot runbook, `.env.example` alignment, prod Actuator verification note, sprint → review.
- **2026-04-08** — Story 7.3 marked **done** after review.

---

**Story completion status:** `done` — completed.
