# Story 4.1: Mentor submission queue

Status: done

<!-- Ultimate context engine analysis completed - comprehensive developer guide created. -->

## Story

As a **mentor**,
I want **a list of submissions that need attention**,
so that **I can work my review queue efficiently**.

## Acceptance Criteria

1. **Queue content** — Given submissions in non-final review states,
   when a mentor opens the queue,
   then rows show **task**, **intern**, **due** context, and status (e.g. awaiting review, in progress) (**FR13**).

2. **Layout** — Given desktop widths,
   when the queue page loads,
   then **Bootstrap** grid supports scanning and ordering **by due date** where available (**UX-DR2**).

3. **A11y baseline** — Keyboard and contrast reasonable for queue as **NFR1** baseline on this flow.

4. **Traceability** — **FR13**, **UX-DR2**, **NFR1** (queue).

## Tasks / Subtasks

- [x] Query: submissions needing mentor attention (define states consistent with **2.4** / review lifecycle).
- [x] `web/review` or `web/queue` controller + `templates/review/queue.html` (illustrative).
- [x] Secure: **MENTOR** (and policy: **ADMIN** if allowed).
- [x] Tests: `@WebMvcTest` with mentor user; intern gets 403.

## Dev Notes

### Prerequisites

- **Epic 3** retrieval status helpful but queue can list items with **pending/failed** retrieval differentiated in later drill-down (**4.2**).

### Architecture compliance

- Controllers delegate to services; templates under `templates/review/` [Source: architecture **Project Structure**].

### Implementation notes

- Queue includes submissions in **`SUBMITTED`** and **`UNDER_REVIEW`** (excludes **`OUTCOME_PUBLISHED`**). Ordered via `SubmissionRepository.findQueuedForMentorReview` (task due date, then `updatedAt`).
- Route: **`GET /review/queue`** (`MentorReviewQueueController`). Retrieval column summarizes git state for scanning.

### References

- [Source: `_bmad-output/planning-artifacts/epics.md` — Story 4.1]

## Dev Agent Record

### Agent Model Used

Cursor (implementation session).

### Debug Log References

—  

### Completion Notes List

- Bootstrap 5 CDN on `review/queue.html`; table-responsive card layout.
- Integration coverage: `Epic4MentorReviewIntegrationTest` (mentor 200, intern 403 on queue).

### File List

- `src/main/java/com/examinai/app/web/review/MentorReviewQueueController.java`
- `src/main/java/com/examinai/app/service/MentorReviewService.java` (queue query delegation)
- `src/main/java/com/examinai/app/domain/task/SubmissionRepository.java`
- `src/main/java/com/examinai/app/domain/task/SubmissionStatus.java`
- `src/main/resources/templates/review/queue.html`
- `src/main/java/com/examinai/app/config/SecurityConfig.java` (`/review/**` rule)
- `src/main/resources/templates/home.html`, `src/main/resources/templates/tasks/list.html` (navigation links)
- `src/test/java/com/examinai/app/web/review/MentorReviewQueueWebMvcTest.java`
- `src/test/java/com/examinai/app/web/Epic4MentorReviewIntegrationTest.java` (queue access)

---

**Story completion status:** `done` — Implemented and verified in test suite.
