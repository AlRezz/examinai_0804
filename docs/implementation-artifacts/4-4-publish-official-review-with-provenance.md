# Story 4.4: Publish official review with provenance

Status: done

<!-- Ultimate context engine analysis completed - comprehensive developer guide created. -->

## Story

As a **mentor**,
I want **to publish my review as the official outcome**,
so that **interns and auditors see my decision—not the model’s**.

## Acceptance Criteria

1. **Transactional publish** — Given valid rubric + narrative per validation,
   when mentor chooses **Publish**,
   then a **transactional application service** records **published outcome**, **publishing mentor identity**, and **timestamp** (**FR17**, **FR27**).

2. **No AI required** — Given no AI draft exists,
   when publishing,
   then publish **succeeds** (**FR21**).

3. **Concurrency / UX** — Given concurrent refresh,
   when publish completes,
   then status transition is consistent—no misleading “final grade” flash before publish completes.

4. **Traceability** — **FR17**, **FR27**, **FR21**; architecture transactional publish.

## Tasks / Subtasks

- [ ] `ReviewService.publish(...)` (name illustrative): one transaction writes published row(s), updates submission/review state, audit fields.
- [ ] **Do not** route publish through controller-direct repository writes [Source: architecture].
- [ ] Controller: POST publish; flash message; redirect.
- [ ] Tests: integration test transactional rollback on forced failure; proves mentor id stored.

## Dev Notes

### Prerequisites

- **4.3** draft fields; distinguish **published** table or columns.

### Architecture compliance

- Published vs AI draft rows **separate** [Source: architecture **Data boundaries**].

### References

- [Source: `_bmad-output/planning-artifacts/epics.md` — Story 4.4]

## Dev Agent Record

### Agent Model Used
_(filled by dev agent)_
### Debug Log References
### Completion Notes List
### File List
_(filled by dev agent on completion)_

---

**Story completion status:** `done` — Ultimate context engine analysis completed.
