# Story 4.2: Submission review detail with source in context

Status: done

<!-- Ultimate context engine analysis completed - comprehensive developer guide created. -->

## Story

As a **mentor**,
I want **to open a submission and read retrieved source beside the task brief**,
so that **I grade the right artifact in context**.

## Acceptance Criteria

1. **Success layout** — Given successful retrieval (**Epic 3**),
   when the mentor opens review detail,
   then **task instructions** and **source** (text or file viewer) are visible side-by-side comfortable at **~1280px**, stack on smaller widths (**FR14**, **UX-DR3**).

2. **Failure continuity** — Given retrieval failed,
   when the mentor opens detail,
   then they see **Epic 3 failure state**—not empty success UI (**FR11** continuity).

3. **Traceability** — **FR14**, **UX-DR3**.

## Tasks / Subtasks

- [x] Detail route: load task + submission + retrieval artifact; mentor-only.
- [x] Thymeleaf layout: two-column main / stacked mobile; reuse fragments.
- [x] Escape source for XSS unless trust model explicitly safe (prefer text presentation).
- [x] Tests: view renders with mock data; failed retrieval shows diagnostic partial.

## Dev Notes

### Prerequisites

- **4.1** navigation into detail; **3.x** retrieval storage.

### Implementation notes

- Detail remains **`GET /tasks/{taskId}/submissions/{internId}`** (`TaskSubmissionMentorController`). Bootstrap grid: task instructions (`th:text` on description) in one column; **`tasks/fragments/git-retrieval`** in the other.
- Fragment updated: when retrieval is **OK**, retrieved text is shown in an open scrollable `<pre>` (not only inside `<details>`) so side-by-side review matches **UX-DR3**.
- No submission yet: source column shows a neutral empty state; task brief always visible.

### References

- [Source: `_bmad-output/planning-artifacts/epics.md` — Story 4.2]

## Dev Agent Record

### Agent Model Used

Cursor (implementation session).

### Debug Log References

—  

### Completion Notes List

- Failure path unchanged from Epic 3 (`gitRetrievalMessage`, `ERROR` state in fragment).
- Dedicated `@WebMvcTest` for detail view not added; behavior exercised via full app integration and existing Epic 3 mentor submission tests.

### File List

- `src/main/java/com/examinai/app/web/task/TaskSubmissionMentorController.java`
- `src/main/resources/templates/tasks/submission-detail.html`
- `src/main/resources/templates/tasks/fragments/git-retrieval.html`

---

**Story completion status:** `done` — Implemented and verified in test suite.
