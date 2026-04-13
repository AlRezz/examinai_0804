# Story 9.3: README and runbook — mentor AI steps alignment

Status: done

## Tasks / Subtasks

- [x] README: steps 5–7 and suggested e2e order aligned with implementation (fetched text → read-only AI → rubric / Save draft / publish)
- [x] Runbook smoke path step 6 already consistent; Traceability lists Epic 9

## Dev Agent Record

### Completion Notes

- README step 7 uses **Save draft** to match the UI button label.

### File List

- `README.md`
- `docs/runbook-pilot.md`

### Change Log

- 2026-04-10: Epic 9.3 — documentation alignment with Epic 9 behavior.

## Story

As an **operator or mentor**,
I want **README and the pilot runbook to describe mentor steps 5–7 consistently**,
so that **documentation matches implementation**: LLM on fetched text → read-only AI output → mentor scores and publish.

## Context

README mentor steps (5–7) and the pilot runbook **smoke path** step 6 were aligned during epic planning (2026-04-10). After **9.1** and **9.2** ship, re-read both docs to confirm copy still matches the implemented UI and LLM contract.

Planning: `_bmad-output/planning-artifacts/epics.md` — Epic 9, Story 9.3.

## Acceptance Criteria

1. **README** — Section *Mentor or administrator — move work through review and publish* lists: (5) LLM call on **fetched** text with **code feedback** and **improvement suggestions**; (6) **read-only** AI display; (7) rubric + mentor feedback, save draft / publish.
2. **Runbook** — `docs/runbook-pilot.md` mentor or smoke sections do not contradict README; update short copy where AI draft is mentioned.
3. **Traceability** — No secrets in docs; optional cross-link to Epic 9 in `_bmad-output/planning-artifacts/epics.md` if maintainers find it useful (minimal change).

## File List (expected touchpoints)

- `README.md`
- `docs/runbook-pilot.md`
