# Story 6.4: Privacy isolation between interns

Status: done

<!-- Ultimate context engine analysis completed - comprehensive developer guide created. -->

## Story

As an **intern**,
I want **guarantees I only see my own outcomes**,
so that **peer grades stay private**.

## Acceptance Criteria

1. **IDOR blocked** — Given another intern’s submission id or deep link,
   when I request it,
   then **403** or **404** per policy; **no** scores or mentor text leaked (**FR25**, **UX-DR10**).

2. **List isolation** — Given shared tasks list policy,
   when I browse,
   then I **never** see other interns’ rubric outcomes or mentor feedback.

3. **Traceability** — **FR25**, **UX-DR10**.

## Tasks / Subtasks

- [ ] Audit **all** intern-facing queries: filter by `currentUser` / assignment ownership.
- [ ] Security tests: user A cannot open user B submission UUID.
- [ ] Optional: randomized 404 vs 403—pick one and document (avoid enumeration if 404 chosen for all).

## Dev Notes

### Prerequisites

- **6.1–6.3** intern surfaces exist—harden each route.

### Architecture compliance

- **Authorization** at service boundary [Source: architecture **Architectural Boundaries**].

### References

- [Source: `_bmad-output/planning-artifacts/epics.md` — Story 6.4]

## Dev Agent Record

### Agent Model Used
_(filled by dev agent)_
### Debug Log References
### Completion Notes List
### File List
_(filled by dev agent on completion)_

---

**Story completion status:** `done` — Ultimate context engine analysis completed.