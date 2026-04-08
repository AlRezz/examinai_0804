# Story 4.1: Mentor submission queue

Status: ready-for-dev

<!-- Ultimate context engine analysis completed - comprehensive developer guide created. -->

## Story

As a **mentor**,
I want **a list of submissions that need attention**,
so that **I can work my review queue efficiently**.

## Acceptance Criteria

1. **Queue content** — Given submissions in non-final review states,
   when a mentor opens the queue,
   then rows show **task**, **intern**, **due** context, and status (e.g. awaiting review, in progress) (**FR13**).

2. **Layout** — Given desktop widths,
   when the queue page loads,
   then **Bootstrap** grid supports scanning and ordering **by due date** where available (**UX-DR2**).

3. **A11y baseline** — Keyboard and contrast reasonable for queue as **NFR1** baseline on this flow.

4. **Traceability** — **FR13**, **UX-DR2**, **NFR1** (queue).

## Tasks / Subtasks

- [ ] Query: submissions needing mentor attention (define states consistent with **2.4** / review lifecycle).
- [ ] `web/review` or `web/queue` controller + `templates/review/queue.html` (illustrative).
- [ ] Secure: **MENTOR** (and policy: **ADMIN** if allowed).
- [ ] Tests: `@WebMvcTest` with mentor user; intern gets 403.

## Dev Notes

### Prerequisites

- **Epic 3** retrieval status helpful but queue can list items with **pending/failed** retrieval differentiated in later drill-down (**4.2**).

### Architecture compliance

- Controllers delegate to services; templates under `templates/review/` [Source: architecture **Project Structure**].

### References

- [Source: `_bmad-output/planning-artifacts/epics.md` — Story 4.1]

## Dev Agent Record

### Agent Model Used
_(filled by dev agent)_
### Debug Log References
### Completion Notes List
### File List
_(filled by dev agent on completion)_

---

**Story completion status:** `ready-for-dev` — Ultimate context engine analysis completed.
