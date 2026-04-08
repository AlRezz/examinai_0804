# Story 6.1: Intern feedback view for published outcomes

Status: done

<!-- Ultimate context engine analysis completed - comprehensive developer guide created. -->

## Story

As an **intern**,
I want **to see published scores and mentor feedback for my submission**,
so that **I know what the mentor decided**.

## Acceptance Criteria

1. **Published feedback visible** — Given a **published** review for **my** submission revision,
   when I open my submission feedback page,
   then mentor-published **rubric scores** and **narrative** appear prominently (**FR22**, **UX-DR5**).

2. **No WIP as final** — Given **no** publish yet,
   when I open the page,
   then I do **not** see unpublished mentor work-in-progress presented as final grades.

3. **Traceability** — **FR22**, **UX-DR5**.

## Tasks / Subtasks

- [x] Route + controller: intern-only; load submission by id with **ownership check** (precursor to **6.4**).
- [x] Thymeleaf: feedback template; bind published outcome fields only.
- [x] Reuse submission lifecycle hook from **6.3** when both land—coordinate attribute names.
- [x] Tests: intern sees own published review; without publish, no fake-final UI.

## Dev Notes

### Prerequisites

- **4.4** published reviews; **2.3–2.4** intern submission context.

### Architecture compliance

- Intern templates under `templates/submissions/` or `intern/` [Source: `_bmad-output/planning-artifacts/architecture.md` — Structure].

### References

- [Source: `_bmad-output/planning-artifacts/epics.md` — Story 6.1]

## Dev Agent Record

### Agent Model Used

Composer (Cursor agent)

### Debug Log References

_(none)_

### Completion Notes List

- Added `GET /intern/submissions/{submissionId}/feedback` with ownership enforced via `SubmissionRepository.findByIdAndIntern_IdWithTask`; cross-tenant access returns **404**.
- Official block shows only `PublishedReview` rows whose snapshot matches the submission’s current commit, fetch version, and path scope (`MentorReviewService.findLatestPublishedForCurrentRevision`).
- Unpublished mentor draft is never exposed on intern pages.

### File List

- `src/main/java/com/examinai/app/web/intern/InternSubmissionFeedbackController.java`
- `src/main/java/com/examinai/app/service/InternFeedbackService.java`
- `src/main/java/com/examinai/app/service/InternFeedbackBundle.java`
- `src/main/java/com/examinai/app/service/MentorReviewService.java`
- `src/main/java/com/examinai/app/domain/task/SubmissionRepository.java`
- `src/main/resources/templates/intern/submissions/feedback.html`
- `src/main/resources/templates/intern/tasks/list.html`
- `src/main/resources/templates/intern/tasks/detail.html`
- `src/test/java/com/examinai/app/web/Epic6InternSurfacesIntegrationTest.java`

## Change Log

- 2026-04-08: Implemented intern feedback page tied to submission id + current-revision published outcome; integration tests for publish visibility and post-resubmit empty official state.

---

**Story completion status:** `done` — Accepted; epic 6 closed in sprint tracking.
