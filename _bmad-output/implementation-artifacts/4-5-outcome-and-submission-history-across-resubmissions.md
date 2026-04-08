# Story 4.5: Outcome and submission history across resubmissions

Status: done

<!-- Ultimate context engine analysis completed - comprehensive developer guide created. -->

## Story

As a **mentor or auditor (read-only in Epic 7)**,
I want **history of outcomes tied to submission revisions**,
so that **resubmissions are explainable over time**.

## Acceptance Criteria

1. **Revision linkage** — Given intern submits revision after feedback,
   when mentor views **case timeline**,
   then prior **published** outcomes remain tied to the **correct submission version** (**FR28**).

2. **Clarity** — UI makes clear **which outcome applies to which revision** (mentor-facing minimum; intern refinement **Epic 6**).

3. **Traceability** — **FR28**.

## Tasks / Subtasks

- [x] Model: **2.4** single row per (task, intern); each **`published_reviews`** row stores **revision snapshots** (`snapshot_commit_sha`, `snapshot_git_fetch_version`, `snapshot_path_scope`) instead of a separate `submission_revision_id` FK.
- [x] Mentor timeline view or section on detail page; query ordered history.
- [x] Tests: two revisions, two publishes—history order correct.

## Dev Notes

### Prerequisites

- **4.4** publish; **2.4** coordinate revisions policy.

### Implementation notes

- **2.4** keeps one **`Submission`** row per (task, intern); revisions = updated coordinates on that row. **Linkage** is **`published_reviews`** snapshot columns: **`snapshot_commit_sha`**, **`snapshot_git_fetch_version`**, **`snapshot_path_scope`** (no separate `submission_revision_id` table).
- **Resubmit**: intern **`SubmissionService.upsertCoordinates`** with **`OUTCOME_PUBLISHED` → `SUBMITTED`** clears **`mentor_review_drafts`** so a new cycle does not reuse stale WIP.
- **UI**: “Published outcome history” section on **`submission-detail.html`**, ordered **`published_at` descending** (repository: `findBySubmission_IdOrderByPublishedAtDesc`), each row shows snapshot + scores + narrative.

### References

- [Source: `_bmad-output/planning-artifacts/epics.md` — Story 4.5]

## Dev Agent Record

### Agent Model Used

Cursor (implementation session).

### Debug Log References

—  

### Completion Notes List

- `Epic4MentorReviewIntegrationTest.publishStoresProvenanceAndHistoryAcrossRevision`: two publishes after coordinate change; asserts history size, snapshot SHAs on rows, and HTML contains both narratives and commits.

### File List

- `src/main/java/com/examinai/app/domain/review/PublishedReview.java`
- `src/main/java/com/examinai/app/domain/review/PublishedReviewRepository.java`
- `src/main/java/com/examinai/app/service/MentorReviewService.java` (`listPublishedHistory`)
- `src/main/java/com/examinai/app/service/SubmissionService.java` (draft clear on resubmit after publish)
- `src/main/java/com/examinai/app/web/task/TaskSubmissionMentorController.java` (history in model)
- `src/main/resources/templates/tasks/submission-detail.html`
- `src/test/java/com/examinai/app/web/Epic4MentorReviewIntegrationTest.java`

---

**Story completion status:** `done` — Implemented and verified in test suite.
