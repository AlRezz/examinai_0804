# Story 3.1: Version-control configuration and secret hygiene

Status: review

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

- [x] Add typed config bean (e.g. `GitProviderProperties`) bound from `GIT_*` or `SPRING_*` env; document in `.env.example` (keys only).
- [x] Ensure **no** Git client usage from controllers—only `integration.git` package.
- [x] Add logging helper or rules: redact query params / headers on failures.
- [x] Tests: properties bind from env; unit test that log lines from simulated failure omit token pattern.

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
Composer (Cursor agent)
### Debug Log References
### Completion Notes List
- Added `GitProviderProperties` with `examinai.git.*` mapped from `GIT_PROVIDER_*` in `application.yml`; `.env.example` documents keys only.
- Introduced `integration.git.LogRedactionUtil` and applied redaction in `GitSourceClient` warnings; 403 paths avoid echoing raw provider bodies in exception messages.
- All Git HTTP remains in `com.examinai.app.integration.git` (`GitSourceClient`); controllers use services only.
### File List
- `src/main/java/com/examinai/app/config/GitProviderProperties.java`
- `src/main/java/com/examinai/app/integration/git/LogRedactionUtil.java`
- `src/main/resources/application.yml`
- `.env.example`
- `src/test/java/com/examinai/app/config/GitProviderPropertiesBindingTest.java`
- `src/test/java/com/examinai/app/integration/git/LogRedactionUtilTest.java`
- `src/test/java/com/examinai/app/integration/git/GitSourceClientTest.java` (shared: safe failure message assertion)
- (shared with 3.2) `GitSourceClient.java`, `GitClientConfig.java`

---

**Story completion status:** `review` — Implementation complete; run `code-review` workflow next.
