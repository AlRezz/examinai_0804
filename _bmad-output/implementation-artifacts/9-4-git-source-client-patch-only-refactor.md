# Story 9.4: GitSourceClient — patch-only text from commit `files[]`

Status: done

## Tasks / Subtasks

- [x] Refactor `buildTextFromCommitResponse` so a matching GitHub `files[]` entry yields **only** `fileNode.path("patch").asText("")` (no commit header, no `File:` line, no `raw_url` / `contents_url` fallbacks for that entry)
- [x] Keep Contents API fallback when path is not in `files[]` or commit has no file list (with path scope set)
- [x] Update `GitSourceClientTest` expectations

## Dev Agent Record

### Completion Notes

- Removed `appendFileBodyFromGithubFileEntry` and related helpers; empty `patch` on a listed file returns `""`.
- Blank path scope with empty `files[]` returns `""` (no commit metadata block).

### File List

- `src/main/java/com/examinai/app/integration/git/GitSourceClient.java`
- `src/test/java/com/examinai/app/integration/git/GitSourceClientTest.java`

### Change Log

- 2026-04-13: Epic 9.4 — patch-only normalization from commit response for `files[]` matches.

## Story

As a **developer consuming fetched source for mentor review and AI**,
I want **`GitSourceClient` to return the unified diff (`patch`) from the GitHub commit payload when the file is listed**,
So that **downstream code receives a single, predictable shape** (diff text only) without redundant commit metadata or implicit raw fetches.

## Context

GitHub’s [get a commit](https://docs.github.com/en/rest/commits/commits) response includes `files[].patch` for changes below a size threshold. Returning only that field keeps LLM and UI input aligned with “diff of the change” rather than a composite blob.

Planning: Epic 9 follow-up; implementation in `integration.git`.

## Acceptance Criteria

1. When the commit JSON includes a `files[]` entry for the resolved path (or the first file when path scope is blank), `fetchNormalizedFileContent` returns **exactly** the `patch` string (may be empty); no leading commit summary, URLs, or `File:` labels.
2. When `patch` is missing or empty on that entry, the implementation does **not** follow `raw_url` or `contents_url` for that entry; the result is the empty patch string.
3. When the requested path is **not** in `files[]`, or the commit has no files but a path scope is set, behavior continues to use the repository Contents API (or existing path) as before, without adding commit-metadata wrappers to the returned body.
4. Size limits (`MAX_RETRIEVED_CHARS`) still apply to returned text.
5. Unit tests in `GitSourceClientTest` cover patch-only output, empty patch, blank path first file, contents fallback, and error paths.

## File List (expected touchpoints)

- `src/main/java/com/examinai/app/integration/git/GitSourceClient.java`
- `src/test/java/com/examinai/app/integration/git/GitSourceClientTest.java`
