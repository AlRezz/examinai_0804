# Story 9.2: Read-only AI draft display on submission detail

Status: done

## Tasks / Subtasks

- [x] Separate **AI assistive draft** vs **Your rubric & narrative**; AI text in `readonly` textarea with label and helper copy
- [x] Empty state when no AI draft (`role="status"`) without blocking rubric
- [x] Integration test expectations updated (`Epic5AiDraftPersistenceIntegrationTest`)

## Dev Agent Record

### Completion Notes

- Submission detail: AI block first (border-bottom), mentor form second; metadata stays in collapsible `<details>`.

### File List

- `src/main/resources/templates/tasks/submission-detail.html`
- `src/test/java/com/examinai/app/web/Epic5AiDraftPersistenceIntegrationTest.java`

### Change Log

- 2026-04-10: Epic 9.2 — read-only AI draft presentation and empty state.

## Story

As a **mentor or administrator**,
I want **the AI assistive draft shown in read-only fields**,
so that **I cannot edit model text in place** and I clearly separate it from my own **quality / readability / correctness** scores and **mentor feedback** (UX-DR4, FR16).

## Context

Submission detail templates under `templates/tasks/` (or equivalent) currently surface draft and rubric; this story ensures AI draft content uses **readonly** or non-editable presentation while mentor inputs remain editable.

Planning: `_bmad-output/planning-artifacts/epics.md` — Epic 9, Story 9.2.

## Acceptance Criteria

1. **Read-only draft** — When an AI draft exists, it is rendered in controls that are not user-editable (e.g. `readonly` textarea with distinct styling, or static preformatted block).
2. **Separation** — Mentor rubric and free-text feedback fields are visually and programmatically distinct from the AI block; labels state that the AI block is **assistive / not official**.
3. **Accessibility** — Read-only draft region has a clear accessible name; focus order remains logical (NFR1 baseline).
4. **No draft** — If no draft was generated, the UI shows an empty or helper state without blocking manual scoring or publish (FR21).

## File List (expected touchpoints)

- `src/main/resources/templates/tasks/submission-detail.html` (and fragments if used)
- Related CSS or Bootstrap utility classes as needed

## References

- README — mentor flow step 6 (read-only AI text).
