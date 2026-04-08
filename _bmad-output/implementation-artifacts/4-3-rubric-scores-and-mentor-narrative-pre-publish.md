# Story 4.3: Rubric scores and mentor narrative (pre-publish)

Status: ready-for-dev

<!-- Ultimate context engine analysis completed - comprehensive developer guide created. -->

## Story

As a **mentor**,
I want **to enter structured scores and my own free-text feedback**,
so that **my judgment is captured before I publish**.

## Acceptance Criteria

1. **WIP persistence** — Given review detail,
   when the mentor adjusts **quality**, **readability**, **correctness** scores and free-text feedback,
   then values persist as **unpublished** work-in-progress (**FR15**, **FR16**).

2. **Validation** — Given invalid numerics,
   when save attempted,
   then errors show accessibly (**NFR1**).

3. **Traceability** — **FR15**, **FR16**.

## Tasks / Subtasks

- [ ] Liquibase: `review_drafts` or columns on review aggregate—**distinct** from **published** outcome tables/columns used in **4.4** (align with architecture: draft vs published separation).
- [ ] Service: save draft; transactional.
- [ ] Form POST + redirect; **CSRF**; Thymeleaf `th:field`.
- [ ] Tests: service persists draft; publish not required yet.

## Dev Notes

### Prerequisites

- **4.2** detail view shell—embed form or separate panel.

### Architecture compliance

- **Mentor publish** and AI draft flows later must use **transactional services** [Source: architecture **Enforcement Guidelines**].

### References

- [Source: `_bmad-output/planning-artifacts/epics.md` — Story 4.3]

## Dev Agent Record

### Agent Model Used
_(filled by dev agent)_
### Debug Log References
### Completion Notes List
### File List
_(filled by dev agent on completion)_

---

**Story completion status:** `ready-for-dev` — Ultimate context engine analysis completed.
