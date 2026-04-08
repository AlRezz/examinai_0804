# Story 2.3: Intern task list

Status: ready-for-dev

<!-- Ultimate context engine analysis completed - comprehensive developer guide created. -->

## Story

As an **intern**,
I want **to see tasks assigned to me**,
so that **I know what I must deliver**.

## Acceptance Criteria

1. **Scoped list** — Given an authenticated intern with at least one assignment (**2.2**),
   when they open their task list,
   then they see **only** their assigned tasks with due information (**FR8**).

2. **Privacy precursor** — Given another intern’s assignments,
   when this intern views their list,
   then they **do not** see tasks assigned only to others (**FR25** precursor).

3. **Traceability** — **FR8**.

## Tasks / Subtasks

- [ ] Query service: tasks for `currentUser.id` via assignment join—**no** broad task leak (AC: #1–#2).
- [ ] `InternTaskController` (or under `web/task`) + Thymeleaf list template (AC: #1).
- [ ] Security: role `INTERN` (and optionally self-only resource rules) (AC: #1–#2).
- [ ] Tests: two interns, assignments differ; each sees only own rows (AC: #2).

## Dev Notes

### Prerequisites

- **2.2** assignment model populated.

### Architecture compliance

- Controllers delegate to services; **no** SQL in controllers [Source: architecture **Boundaries**].

### Out of scope

- Submission attachment (**2.4**).

### References

- [Source: `_bmad-output/planning-artifacts/epics.md` — Story 2.3]

## Dev Agent Record

### Agent Model Used
_(filled by dev agent)_
### Debug Log References
### Completion Notes List
### File List
_(filled by dev agent on completion)_

---

**Story completion status:** `ready-for-dev` — Ultimate context engine analysis completed.
