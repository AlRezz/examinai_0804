# Story 10.2: Mentor submission detail — “Fetched source” after Git fetch

Status: done

## Tasks / Subtasks

- [x] Add **Fetched source** card on `tasks/submission-detail.html` after fetch control when retrieval **OK** and text non-empty
- [x] Remove duplicate **Retrieved text** block from `tasks/fragments/git-retrieval.html` (status remains in **Source retrieval** column)
- [x] Align path scope / NOT_FOUND help copy with patch-first + contents fallback

## Dev Agent Record

### File List

- `src/main/resources/templates/tasks/submission-detail.html`
- `src/main/resources/templates/tasks/fragments/git-retrieval.html`

### Change Log

- 2026-04-13: Epic 10.2 — prominent fetched source for mentors; fragment text deduplicated.

## Story

As a **mentor**,
I want **fetched Git text shown in the main review flow right after I run fetch**,
So that **I can read patch/file content while scoring without scrolling away from actions**.

## Acceptance Criteria

1. After successful fetch, non-empty `gitRetrievedText` appears in a **Fetched source** card (monospace, bounded height, scroll).
2. Git retrieval fragment still shows status/diagnostics; full body text is not duplicated in the side column.
