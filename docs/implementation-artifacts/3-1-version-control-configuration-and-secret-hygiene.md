# Story 3.1: Version-control configuration and secret hygiene

Status: done

<!-- Ultimate context engine analysis completed - comprehensive developer guide created. -->

## Story

As an **operator or administrator**,
I want **Git host parameters and tokens supplied via deployment configuration—not user-visible fields**,
so that **tokens never appear in the UI or logs**.

## Acceptance Criteria

1. **Config-only credentials** — Given env vars or Compose secrets for provider base URL and token,
   when the application starts,
   then **integration** code reads credentials **only** from `@ConfigurationProperties` / beans—**never** from request params, user profile DB fields, or UI (**FR31**).

2. **Safe logging** — Given a failed Git call,
   when the system logs the event,
   then logs contain **no** token values, full secret-bearing URLs, or raw provider error bodies that echo secrets (**NFR12**).

3. **Traceability** — **FR31**, **NFR12**; aligns with architecture **Secrets** and **integration.git** boundary.

## Tasks / Subtasks

- [ ] Add typed config bean (e.g. `GitProviderProperties`) bound from `GIT_*` or `SPRING_*` env; document in `.env.example` (keys only).
- [ ] Ensure **no** Git client usage from controllers—only `integration.git` package.
- [ ] Add logging helper or rules: redact query params / headers on failures.
- [ ] Tests: properties bind from env; unit test that log lines from simulated failure omit token pattern.

## Dev Notes

### Prerequisites

- **Epic 2** submissions with coordinates exist.

### Architecture compliance

- Secrets: **Compose/env only**; never plaintext in DB [Source: `_bmad-output/planning-artifacts/architecture.md` — Authentication & Security, API boundaries].
- **One** HTTP client style (**WebClient** or **RestClient**)—**lock in story 3.2** when implementing the client bean; this story can prepare the config module only.

### Out of scope

- Actual HTTP fetch (**3.2**); mentor retry UX (**3.3**).

### References

- [Source: `_bmad-output/planning-artifacts/epics.md` — Story 3.1]

## Dev Agent Record

### Agent Model Used
_(filled by dev agent)_
### Debug Log References
### Completion Notes List
### File List
_(filled by dev agent on completion)_

---

**Story completion status:** `done` — Ultimate context engine analysis completed.
