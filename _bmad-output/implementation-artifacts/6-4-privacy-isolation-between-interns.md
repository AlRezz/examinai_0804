# Story 6.4: Privacy isolation between interns

Status: done

<!-- Ultimate context engine analysis completed - comprehensive developer guide created. -->

## Story

As an **intern**,
I want **guarantees I only see my own outcomes**,
so that **peer grades stay private**.

## Acceptance Criteria

1. **IDOR blocked** — Given another intern’s submission id or deep link,
   when I request it,
   then **403** or **404** per policy; **no** scores or mentor text leaked (**FR25**, **UX-DR10**).

2. **List isolation** — Given shared tasks list policy,
   when I browse,
   then I **never** see other interns’ rubric outcomes or mentor feedback.

3. **Traceability** — **FR25**, **UX-DR10**.

## Tasks / Subtasks

- [x] Audit **all** intern-facing queries: filter by `currentUser` / assignment ownership.
- [x] Security tests: user A cannot open user B submission UUID.
- [x] Optional: randomized 404 vs 403—pick one and document (avoid enumeration if 404 chosen for all).

## Dev Notes

### Prerequisites

- **6.1–6.3** intern surfaces exist—harden each route.

### Architecture compliance

- **Authorization** at service boundary [Source: architecture **Architectural Boundaries**].

### Policy (this implementation)

- Intern feedback URLs use **`404 Not Found`** for unknown submission ids and for ids owned by another intern (uniform response to reduce id enumeration).

### References

- [Source: `_bmad-output/planning-artifacts/epics.md` — Story 6.4]

## Dev Agent Record

### Agent Model Used

Composer (Cursor agent)

### Debug Log References

_(none)_

### Completion Notes List

- Intern routes audited: `/intern/tasks` (assignments only), `/intern/tasks/{taskId}` (assignment guard), `/intern/submissions/{id}/feedback` (`findByIdAndIntern_IdWithTask`).
- Integration test: intern A receives 404 when requesting intern B’s submission feedback URL.

### File List

- `src/main/java/com/examinai/app/domain/task/SubmissionRepository.java` (`findByIdAndIntern_Id`, `findByIdAndIntern_IdWithTask`)
- `src/main/java/com/examinai/app/service/InternFeedbackService.java`
- `src/main/java/com/examinai/app/web/intern/InternSubmissionFeedbackController.java`
- `src/test/java/com/examinai/app/web/Epic6InternSurfacesIntegrationTest.java`

## Change Log

- 2026-04-08: Submission ownership repository guards + IDOR integration test; documented 404 policy.

---

**Story completion status:** `done` — Accepted; epic 6 closed in sprint tracking.
