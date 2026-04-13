# Story 6.3: Submission lifecycle status

Status: done

<!-- Ultimate context engine analysis completed - comprehensive developer guide created. -->

## Story

As an **intern**,
I want **to see where my submission sits in the review lifecycle**,
so that **I know whether I should wait or revise**.

## Acceptance Criteria

1. **Status display** — Given MVP-defined states (e.g. submitted, retrieval failed, awaiting review, in review, published),
   when I view my submission,
   then **current** status shows **consistently** across refreshes (**FR24**, **UX-DR6**).

2. **Single source of truth** — Status derived from submission + retrieval + review state; document mapping in service layer.

3. **Traceability** — **FR24**, **UX-DR6**.

## Tasks / Subtasks

- [x] Domain/service: compute `SubmissionLifecycleStatus` from existing tables (no fantasy states).
- [x] Thymeleaf fragment for status badge; include on intern task/submission views.
- [x] Tests: state transitions—mock aggregate shows expected label after publish.

## Dev Notes

### Prerequisites

- **Epic 3** retrieval status; **4.x** review states.

### References

- [Source: `_bmad-output/planning-artifacts/epics.md` — Story 6.3]

## Dev Agent Record

### Agent Model Used

Composer (Cursor agent)

### Debug Log References

_(none)_

### Completion Notes List

- `SubmissionLifecycleService` documents precedence in Javadoc and maps `Submission` + `GitRetrievalState` + `SubmissionStatus` to `SubmissionLifecycleView` (label + Bootstrap badge class).
- Fragment `intern/fragments/submission-lifecycle-badge.html` included on task list, task detail, and feedback pages; model attribute name **`submissionLifecycle`** for reuse.

### File List

- `src/main/java/com/examinai/app/service/SubmissionLifecycleStatus.java`
- `src/main/java/com/examinai/app/service/SubmissionLifecycleView.java`
- `src/main/java/com/examinai/app/service/SubmissionLifecycleService.java`
- `src/main/java/com/examinai/app/web/intern/InternTaskController.java`
- `src/main/resources/templates/intern/fragments/submission-lifecycle-badge.html`
- `src/main/resources/templates/intern/tasks/list.html`
- `src/main/resources/templates/intern/tasks/detail.html`
- `src/main/resources/templates/intern/submissions/feedback.html`
- `src/test/java/com/examinai/app/service/SubmissionLifecycleServiceTest.java`
- `src/test/java/com/examinai/app/web/Epic6InternSurfacesIntegrationTest.java`

## Change Log

- 2026-04-08: Lifecycle computation service + badge fragment on intern surfaces; unit + integration coverage.

---

**Story completion status:** `done` — Accepted; epic 6 closed in sprint tracking.
