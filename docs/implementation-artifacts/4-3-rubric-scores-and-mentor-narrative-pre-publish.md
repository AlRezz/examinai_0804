# Story 4.3: Rubric scores and mentor narrative (pre-publish)

Status: done

<!-- Ultimate context engine analysis completed - comprehensive developer guide created. -->

## Story

As a **mentor**,
I want **to enter structured scores and my own free-text feedback**,
so that **my judgment is captured before I publish**.

## Acceptance Criteria

1. **WIP persistence** — Given review detail,
   when the mentor adjusts **quality**, **readability**, **correctness** scores and free-text feedback,
   then values persist as **unpublished** work-in-progress (**FR15**, **FR16**).

2. **Validation** — Given invalid numerics,
   when save attempted,
   then errors show accessibly (**NFR1**).

3. **Traceability** — **FR15**, **FR16**.

## Tasks / Subtasks

- [x] Liquibase: `review_drafts` or columns on review aggregate—**distinct** from **published** outcome tables/columns used in **4.4** (align with architecture: draft vs published separation).
- [x] Service: save draft; transactional.
- [x] Form POST + redirect; **CSRF**; Thymeleaf `th:field`.
- [x] Tests: draft + mentor flow covered via integration (`Epic4MentorReviewIntegrationTest`); no isolated `@DataJpaTest` for draft-only.

## Dev Notes

### Prerequisites

- **4.2** detail view shell—embed form or separate panel.

### Architecture compliance

- **Mentor publish** and AI draft flows later must use **transactional services** [Source: architecture **Enforcement Guidelines**].

### Implementation notes

- Table **`mentor_review_drafts`** (Liquibase `005-epic4-mentor-review.yaml`), entity **`MentorReviewDraft`**, 1:1 with **`Submission`**. Published outcomes live in **`published_reviews`** (Story **4.4**).
- **`MentorReviewService.saveDraft`**: upserts draft; moves **`SUBMITTED` → `UNDER_REVIEW`** when first saved from submitted state.
- Draft save allows partial scores/feedback; **strict validation (1–5 scores, non-blank narrative)** applies on **publish** in the same service (Story **4.4**). Invalid publish surfaces **`reviewError`** flash on redirect.
- Form: combined actions via **`formaction`** on **Save draft** vs **Publish**; see `submission-detail.html`.

### References

- [Source: `_bmad-output/planning-artifacts/epics.md` — Story 4.3]

## Dev Agent Record

### Agent Model Used

Cursor (implementation session).

### Debug Log References

—  

### Completion Notes List

- Draft persistence exercised through mentor flow in `Epic4MentorReviewIntegrationTest` (publish path implies draft/readiness); no standalone `@DataJpaTest` for draft-only save.

### File List

- `src/main/resources/db/changelog/changes/005-epic4-mentor-review.yaml`
- `src/main/java/com/examinai/app/domain/review/MentorReviewDraft.java`
- `src/main/java/com/examinai/app/domain/review/MentorReviewDraftRepository.java`
- `src/main/java/com/examinai/app/service/MentorReviewService.java`
- `src/main/java/com/examinai/app/web/task/MentorReviewForm.java`
- `src/main/java/com/examinai/app/web/task/TaskSubmissionMentorController.java` (`POST .../review-draft`)
- `src/main/resources/templates/tasks/submission-detail.html`

---

**Story completion status:** `done` — Implemented and verified in test suite.
