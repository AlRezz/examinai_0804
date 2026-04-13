# Story 9.1: LLM draft from fetched source with code feedback and improvement suggestions

Status: done

## Tasks / Subtasks

- [x] Tighten system prompt: `## Feedback on the code` and `## Suggestions to improve` (Markdown h2), keep fetch guards in `AiDraftPayloadLoader`
- [x] Unit test: verify system prompt contains required headings (`AiDraftAssessmentServiceTest`)

## Dev Agent Record

### Completion Notes

- `AiDraftAssessmentService` system instructions now require the two sections in order; resilience unchanged (Epic 5 properties + executor).
- Fetch prerequisite remains enforced in `AiDraftPayloadLoader` (OK state + non-empty retrieved text).

### File List

- `src/main/java/com/examinai/app/integration/ai/AiDraftAssessmentService.java`
- `src/test/java/com/examinai/app/integration/ai/AiDraftAssessmentServiceTest.java`
- `src/main/java/com/examinai/app/integration/git/GitSourceClient.java` (resolve `files[]` by path scope; blank path uses first listed file, or metadata only if `files` is empty; `File:` line for review context)

### Change Log

- 2026-04-10: Epic 9.1 — structured LLM draft contract and test assertion for system prompt; Git client path matching fix required for full `mvn verify`.

## Story

As a **mentor or administrator**,
I want **Generate AI draft to call the LLM with the fetched submission text**,
so that **the assistive draft includes feedback on the code and suggestions to improve**, persisted separately from the official published review (FR18, FR19).

## Context

Epic 5 delivered Spring AI draft generation (`AiDraftAssessmentService`, `AiDraftPayloadLoader`). This story tightens the **contract**: the model output must clearly cover **code feedback** and **improvement suggestions**, built from **successful fetch** only.

Planning: `_bmad-output/planning-artifacts/epics.md` — Epic 9, Story 9.1.

## Acceptance Criteria

1. **Fetch prerequisite** — AI draft generation runs only when normalized fetched source for the submission is available (reuse existing guards/messages; no LLM call on empty payload).
2. **Prompt / template** — System or user instructions require two explicit parts: **Feedback on the code** and **Suggestions to improve** (headings, sections, or structured format agreed in implementation).
3. **Persistence** — Saved draft text and metadata remain in AI-draft storage paths, not merged into published mentor outcome rows (FR19, FR20).
4. **Resilience** — Timeouts, retries, and degraded inference behavior stay consistent with Epic 5 (`AiDraftAssessmentProperties`, bounded calls).

## File List (expected touchpoints)

- `src/main/java/com/examinai/app/integration/ai/AiDraftAssessmentService.java`
- `src/main/java/com/examinai/app/integration/ai/AiDraftPayloadLoader.java`
- Related tests under `src/test/java/.../integration/ai/`

## References

- README — *Mentor or administrator — move work through review and publish*, step 5.
