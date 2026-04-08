# Story 6.3: Submission lifecycle status

Status: ready-for-dev

<!-- Ultimate context engine analysis completed - comprehensive developer guide created. -->

## Story

As an **intern**,
I want **to see where my submission sits in the review lifecycle**,
so that **I know whether I should wait or revise**.

## Acceptance Criteria

1. **Status display** — Given MVP-defined states (e.g. submitted, retrieval failed, awaiting review, in review, published),
   when I view my submission,
   then **current** status shows **consistently** across refreshes (**FR24**, **UX-DR6**).

2. **Single source of truth** — Status derived from submission + retrieval + review state; document mapping in service layer.

3. **Traceability** — **FR24**, **UX-DR6**.

## Tasks / Subtasks

- [ ] Domain/service: compute `SubmissionLifecycleStatus` from existing tables (no fantasy states).
- [ ] Thymeleaf fragment for status badge; include on intern task/submission views.
- [ ] Tests: state transitions—mock aggregate shows expected label after publish.

## Dev Notes

### Prerequisites

- **Epic 3** retrieval status; **4.x** review states.

### References

- [Source: `_bmad-output/planning-artifacts/epics.md` — Story 6.3]

## Dev Agent Record

### Agent Model Used
_(filled by dev agent)_
### Debug Log References
### Completion Notes List
### File List
_(filled by dev agent on completion)_

---

**Story completion status:** `ready-for-dev` — Ultimate context engine analysis completed.
