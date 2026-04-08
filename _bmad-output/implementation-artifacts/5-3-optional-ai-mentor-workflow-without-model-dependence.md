# Story 5.3: Optional AI—mentor workflow without model dependence

Status: done

<!-- Ultimate context engine analysis completed - comprehensive developer guide created. -->

## Story

As a **mentor**,
I want **to ignore or skip AI drafts entirely**,
so that **I am never forced to rely on the model**.

## Acceptance Criteria

1. **Publish without draft** — Given no draft generated,
   when mentor completes scoring and publishes (**4.4**),
   then behavior matches Epic 4; **no** AI fields required (**FR21**).

2. **Override draft** — Given draft exists,
   when mentor overrides suggestions and publishes,
   then **published** reflects mentor values; draft visibility for intern follows policy (**Epic 6**).

3. **Traceability** — **FR21**.

## Tasks / Subtasks

- [x] Audit **4.4** publish path: no dependency on draft row.
- [x] UI: draft section optional / collapsed when unknown; no blocking validation on AI fields.
- [x] Tests: publish with zero drafts succeeds integration-style.

## Dev Notes

### Prerequisites

- **5.2** draft persistence; **4.4** publish.

### Current codebase (after 5.3)

- **4.4** `MentorReviewService.publish` does not read AI tables; only `mentor_review_drafts` is cleared on publish (cleanup, not a read dependency).
- Mentor rubric and publish validate only scores + narrative; no AI fields on `MentorReviewForm`.
- AI assist UI: optional “Generate AI draft” action; latest AI text only in `submission-detail.html` when `latestAiDraft` exists, in a **collapsed** `<details>` block plus copy that publish uses only the rubric below.
- Regression: `Epic5AiDraftPersistenceIntegrationTest.publishReviewViaWebSucceedsWithNoAiInvocationRows` (no `model_invocations`); existing test still asserts mentor narrative wins when an AI draft row exists (AC2 mentor side). **Intern** draft vs published visibility is **Epic 6** (AC2 policy).

### References

- [Source: `_bmad-output/planning-artifacts/epics.md` — Story 5.3]

## Dev Agent Record

### Agent Model Used

Composer (Cursor agent).

### Debug Log References

—  

### Completion Notes List

- Confirmed `MentorReviewService.publish` and `TaskSubmissionMentorController.publishReview` use only form scores + narrative; `deleteBySubmission_Id` is best-effort cleanup of mentor review draft, not a read dependency. No AI tables or `latestAiDraft` in publish path.
- Mentor UI: AI draft panel is optional (only when a persisted AI draft exists), collapsed by default via `<details>`, with explicit copy that publishing uses only rubric fields below.
- `Epic5AiDraftPersistenceIntegrationTest.publishReviewViaWebSucceedsWithNoAiInvocationRows` exercises HTTP publish with zero `model_invocations` rows; `aiDraftAndInvocationPersistedSeparatelyFromPublishedReview` already covers mentor narrative winning over AI text when a draft exists (AC2).

### File List

- `src/main/resources/templates/tasks/submission-detail.html`
- `src/test/java/com/examinai/app/web/Epic5AiDraftPersistenceIntegrationTest.java`
- `_bmad-output/implementation-artifacts/sprint-status.yaml`
- `_bmad-output/implementation-artifacts/5-3-optional-ai-mentor-workflow-without-model-dependence.md` (this story)

## Change Log

- 2026-04-08: Story 5.3 — optional AI mentor workflow UX + regression test for publish without AI rows.
- 2026-04-08: Dev Notes and File List refreshed (removed stale “remaining work”; clarified Epic 6 scope for AC2 intern policy).
- 2026-04-08: Marked **done** after review sign-off.

---

**Story completion status:** `done` — Accepted; Epic 5 story 5.3 closed.
