# Story 4.5: Outcome and submission history across resubmissions

Status: done

<!-- Ultimate context engine analysis completed - comprehensive developer guide created. -->

## Story

As a **mentor or auditor (read-only in Epic 7)**,
I want **history of outcomes tied to submission revisions**,
so that **resubmissions are explainable over time**.

## Acceptance Criteria

1. **Revision linkage** — Given intern submits revision after feedback,
   when mentor views **case timeline**,
   then prior **published** outcomes remain tied to the **correct submission version** (**FR28**).

2. **Clarity** — UI makes clear **which outcome applies to which revision** (mentor-facing minimum; intern refinement **Epic 6**).

3. **Traceability** — **FR28**.

## Tasks / Subtasks

- [ ] Model: submission versions / resubmission chain if not already from **2.4**; link `published_review` → `submission_revision_id`.
- [ ] Mentor timeline view or section on detail page; query ordered history.
- [ ] Tests: two revisions, two publishes—history order correct.

## Dev Notes

### Prerequisites

- **4.4** publish; **2.4** coordinate revisions policy.

### References

- [Source: `_bmad-output/planning-artifacts/epics.md` — Story 4.5]

## Dev Agent Record

### Agent Model Used
_(filled by dev agent)_
### Debug Log References
### Completion Notes List
### File List
_(filled by dev agent on completion)_

---

**Story completion status:** `done` — Ultimate context engine analysis completed.
