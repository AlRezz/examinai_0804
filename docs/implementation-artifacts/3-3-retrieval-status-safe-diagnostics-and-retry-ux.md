# Story 3.3: Retrieval status, safe diagnostics, and retry UX

Status: done

<!-- Ultimate context engine analysis completed - comprehensive developer guide created. -->

## Story

As a **mentor or authorized user**,
I want **clear status for fetch success or failure and a way to retry or fix coordinates**,
so that **I can recover without guessing**.

## Acceptance Criteria

1. **Secret-safe errors** — Given failed retrieval (403, 404, scope error, timeout),
   when a mentor views the submission,
   then they see an **actionable**, **secret-safe** message (**FR11**, **UX-DR8**).

2. **Retry / coordinate fix** — Given failed or stale retrieval,
   when an authorized user triggers **re-fetch** or updates coordinates per policy,
   then the system attempts retrieval again and updates status (**FR12**).

3. **Data integrity on retry** — Given a previous successful snapshot,
   when retry fails,
   then prior good state is **not** silently corrupted (explicit versioning or status flags).

4. **Traceability** — **FR11**, **FR12**, **UX-DR8**.

## Tasks / Subtasks

- [x] Surface retrieval status on mentor review/submission views; map `GitProviderException` (etc.) to stable user-facing codes/messages.
- [x] Controller actions: retry fetch (POST + redirect), coordinate update wired to **3.2** service (**CSRF**).
- [x] Thymeleaf fragments for success / failure / in-progress.
- [x] Tests: web slice—403/404 mapped; no token substrings in model error text.

## Dev Notes

### Prerequisites

- **3.2** fetch pipeline.

### Architecture compliance

- Provider errors → domain failures with **safe** messages [Source: architecture **Process Patterns — Error handling**].

### References

- [Source: `_bmad-output/planning-artifacts/epics.md` — Story 3.3]

## Dev Agent Record

### Agent Model Used
Composer (Cursor agent)
### Debug Log References
### Completion Notes List
- **`TaskSubmissionMentorController`**: `/tasks/{taskId}/submissions` hub, per-intern detail, POST `coordinates` (mentor bypasses intern assignment check via `SubmissionService.mentorUpsertCoordinates`), POST `fetch` calls `SourceRetrievalService` with CSRF tokens on forms.
- **`GitRetrievalUiMessage`** maps persisted `GitFailureKind` names to stable mentor-safe copy; fragments show OK / ERROR / NOT_STARTED / IN_PROGRESS.
- Integration test **`Epic3GitMentorIntegrationTest`** covers mentor access, intern forbidden, and HTML after failed fetch omits token-like substrings.
### File List
- `src/main/java/com/examinai/app/web/task/TaskSubmissionMentorController.java`
- `src/main/java/com/examinai/app/web/task/GitRetrievalUiMessage.java`
- `src/main/java/com/examinai/app/service/SubmissionService.java`
- `src/main/resources/templates/tasks/submissions.html`
- `src/main/resources/templates/tasks/submission-detail.html`
- `src/main/resources/templates/tasks/fragments/git-retrieval.html`
- `src/main/resources/templates/tasks/list.html`
- `src/test/java/com/examinai/app/web/Epic3GitMentorIntegrationTest.java`

---

**Story completion status:** `done` — Marked done; aligns with `sprint-status.yaml`.
