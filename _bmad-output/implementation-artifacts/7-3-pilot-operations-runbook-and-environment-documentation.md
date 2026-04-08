# Story 7.3: Pilot operations runbook and environment documentation

Status: ready-for-dev

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

- [ ] Add/update `.env.example` at repo root; cross-check **3.1** Git vars, datasource, Spring AI.
- [ ] `docs/runbook-pilot.md` (or `RUNBOOK.md`) — short operator-focused.
- [ ] Verify `application-prod.yml` (or pilot) hides sensitive Actuator; link to architecture ADR.
- [ ] Optional: CI checklist snippet.

## Dev Notes

### Prerequisites

- **7.2** Compose; stable health and degraded paths from **5.4**.

### References

- [Source: `_bmad-output/planning-artifacts/epics.md` — Story 7.3]

## Dev Agent Record

### Agent Model Used
_(filled by dev agent)_
### Debug Log References
### Completion Notes List
### File List
_(filled by dev agent on completion)_

---

**Story completion status:** `ready-for-dev` — Ultimate context engine analysis completed.
