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

- [ ] Audit **4.4** publish path: no dependency on draft row.
- [ ] UI: draft section optional / collapsed when unknown; no blocking validation on AI fields.
- [ ] Tests: publish with zero drafts succeeds integration-style.

## Dev Notes

### Prerequisites

- **5.2** draft persistence; **4.4** publish.

### Current codebase (after 5.2)

- **4.4** `MentorReviewService.publish` does not read AI tables; only `mentor_review_drafts` is cleared on publish.
- AI text appears only when `latestAiDraft` is present; mentor rubric and publish have no validation tied to AI.
- Remaining story work: explicit regression tests for “publish with zero AI rows” and UX polish (optional collapse / copy) if product wants it.

### References

- [Source: `_bmad-output/planning-artifacts/epics.md` — Story 5.3]

## Dev Agent Record

### Agent Model Used
_(filled by dev agent)_
### Debug Log References
### Completion Notes List
### File List
_(filled by dev agent on completion)_

---

**Story completion status:** `done` — Ultimate context engine analysis completed.
