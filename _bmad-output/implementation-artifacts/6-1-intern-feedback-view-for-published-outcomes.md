# Story 6.1: Intern feedback view for published outcomes

Status: ready-for-dev

<!-- Ultimate context engine analysis completed - comprehensive developer guide created. -->

## Story

As an **intern**,
I want **to see published scores and mentor feedback for my submission**,
so that **I know what the mentor decided**.

## Acceptance Criteria

1. **Published feedback visible** — Given a **published** review for **my** submission revision,
   when I open my submission feedback page,
   then mentor-published **rubric scores** and **narrative** appear prominently (**FR22**, **UX-DR5**).

2. **No WIP as final** — Given **no** publish yet,
   when I open the page,
   then I do **not** see unpublished mentor work-in-progress presented as final grades.

3. **Traceability** — **FR22**, **UX-DR5**.

## Tasks / Subtasks

- [ ] Route + controller: intern-only; load submission by id with **ownership check** (precursor to **6.4**).
- [ ] Thymeleaf: feedback template; bind published outcome fields only.
- [ ] Reuse submission lifecycle hook from **6.3** when both land—coordinate attribute names.
- [ ] Tests: intern sees own published review; without publish, no fake-final UI.

## Dev Notes

### Prerequisites

- **4.4** published reviews; **2.3–2.4** intern submission context.

### Architecture compliance

- Intern templates under `templates/submissions/` or `intern/` [Source: `_bmad-output/planning-artifacts/architecture.md` — Structure].

### References

- [Source: `_bmad-output/planning-artifacts/epics.md` — Story 6.1]

## Dev Agent Record

### Agent Model Used
_(filled by dev agent)_
### Debug Log References
### Completion Notes List
### File List
_(filled by dev agent on completion)_

---

**Story completion status:** `ready-for-dev` — Ultimate context engine analysis completed.
