# Story 9.6: AI draft — rubric scores (1–5) and auto-fill mentor review form

Status: done

## Tasks / Subtasks

- [x] Extend `AiDraftAssessmentService` system prompt: **Quality**, **Readability**, **Correctness** (each **1–5**) plus existing **Feedback on the code** / **Suggestions to improve** sections
- [x] Parse model output into `AiDraftAssessmentResult` (full text for audit + scores + narrative for mentor feedback field)
- [x] On successful **Generate AI draft**, persist full assessment text as today and **`MentorReviewService.saveDraft`** with parsed scores and narrative so the submission detail form shows filled rubric and feedback

## Dev Agent Record

### Completion Notes

- `generateDraft` returns `AiDraftAssessmentResult`; `TaskSubmissionMentorController` requires authenticated mentor and calls `saveDraft` after persistence.
- Parser accepts score lines in flexible order; narrative prefers content under `## Feedback on the code` and `## Suggestions to improve`, else remainder after scores.

### File List

- `src/main/java/com/examinai/app/integration/ai/AiDraftAssessmentResult.java`
- `src/main/java/com/examinai/app/integration/ai/AiDraftAssessmentResponseParser.java`
- `src/main/java/com/examinai/app/integration/ai/AiDraftAssessmentService.java`
- `src/main/java/com/examinai/app/web/task/TaskSubmissionMentorController.java`
- `src/test/java/com/examinai/app/integration/ai/AiDraftAssessmentResponseParserTest.java`
- `src/test/java/com/examinai/app/integration/ai/AiDraftAssessmentServiceTest.java`
- `src/test/java/com/examinai/app/web/task/TaskSubmissionMentorAiDraftWebMvcTest.java`

### Change Log

- 2026-04-13: Epic 9.6 — structured rubric lines in AI output and mentor draft auto-fill.

## Story

As a **mentor**,
I want **the AI draft response to include suggested rubric scores (1–5) and for those values plus narrative feedback to populate my review form after generation**,
So that **I can start from the model’s proposal and still edit before save or publish**.

## Context

Epic 9 already requires structured **code feedback** and **suggestions** in the assistive draft. This story aligns the model output with the **same three dimensions** as the mentor rubric (FR15) and reduces manual transcription.

## Acceptance Criteria

1. System prompt instructs the model to output **Quality**, **Readability**, and **Correctness** on dedicated lines (digits **1–5**) and to keep the two Markdown sections for feedback and suggestions.
2. `AiDraftAssessmentService` returns a structured result; the **full** model text remains stored on **`AiDraft`** / invocation audit as before.
3. After a successful **Generate AI draft**, **Quality**, **Readability**, **Correctness**, and **mentor feedback** fields on the submission detail page reflect parsed values from the response (via **`mentor_review_drafts`**).
4. Unit tests cover parsing and controller wiring; existing failure paths (inference unavailable) unchanged.

## File List (expected touchpoints)

- `src/main/java/com/examinai/app/integration/ai/AiDraftAssessmentService.java`
- `src/main/java/com/examinai/app/web/task/TaskSubmissionMentorController.java`
- Tests under `src/test/java/com/examinai/app/integration/ai/` and `.../web/task/`
