# Story 10.1: Intern “Your feedback” — official only + score-based card tone

Status: done

## Tasks / Subtasks

- [x] Remove AI draft panel from `intern/submissions/feedback.html`; stop loading draft for intern bundle (`InternFeedbackService`)
- [x] Add `OfficialFeedbackCardSupport` — CSS classes from average of three rubric scores (&lt; 2.5 rose, 2.5–4 yellow, &gt; 4 green)
- [x] Wire controller + template `th:classappend`; unit tests for tone helper
- [x] Update `Epic6InternSurfacesIntegrationTest` expectations

## Dev Agent Record

### File List

- `src/main/java/com/examinai/app/web/intern/OfficialFeedbackCardSupport.java`
- `src/main/java/com/examinai/app/web/intern/InternSubmissionFeedbackController.java`
- `src/main/java/com/examinai/app/service/InternFeedbackService.java`
- `src/main/resources/templates/intern/submissions/feedback.html`
- `src/main/java/com/examinai/app/config/InternUiProperties.java` (javadoc)
- `src/main/resources/application.yml` (comment)
- `src/test/java/com/examinai/app/web/intern/OfficialFeedbackCardSupportTest.java`
- `src/test/java/com/examinai/app/web/Epic6InternSurfacesIntegrationTest.java`

### Change Log

- 2026-04-13: Epic 10.1 — official-only intern feedback; rose/yellow/green card by rubric average.

## Story

As an **intern**,
I want **only official mentor feedback on “Your feedback” with a card color that reflects how strong the published rubric average is**,
So that **assistive AI does not look like a grade and I can scan outcomes quickly**.

## Acceptance Criteria

1. No persisted AI draft content on the intern feedback page.
2. Official feedback card background/border follows average of quality, readability, correctness: **&lt; 2.5** rose, **2.5–4** yellow, **&gt; 4** green.
3. Styling uses existing Bootstrap stack plus small scoped CSS (Epic 8 WebJars baseline).
