# Story 2.1: Create and edit tasks

Status: done

<!-- Ultimate context engine analysis completed - comprehensive developer guide created. -->

## Story

As a **mentor or administrator**,
I want **to create and edit tasks with instructions and due expectations**,
so that **interns know what to build and by when**.

## Acceptance Criteria

1. **Authorization** — Given a user with **mentor** or **administrator** role (per policy),
   when they access task create/edit flows,
   then they are allowed; other roles are denied appropriately.

2. **Persist task** — Given valid input (title, description/instructions, due date—fields as MVP requires),
   when create/update is submitted,
   then the task is persisted and appears on a **mentor/admin task index**.

3. **Validation UX** — Given invalid input,
   when the form is submitted,
   then validation errors display on the form without exposing internal stack details (**NFR6**).

4. **Traceability** — **FR7**.

## Tasks / Subtasks

- [ ] Liquibase: `tasks` table (snake_case columns, `due_at` or similar) + changelog include (AC: #2).
- [ ] Entity `Task`, repository, `TaskService` in `domain.task` (AC: #2).
- [ ] `TaskController` under `web/task` (or `web/tasks`): list, new, edit, form POST + redirect [Source: architecture **Format Patterns**] (AC: #1–#3).
- [ ] Thymeleaf templates under `templates/tasks/` (AC: #2–#3).
- [ ] Secure routes with Spring Security expressions matching mentor/admin (AC: #1).
- [ ] Tests: service + `@WebMvcTest` or slice tests (AC: #1–#3).

## Dev Notes

### Prerequisites

- **Epic 1** authentication and roles (**1.4–1.5**) so mentor/admin identity is meaningful.

### Architecture compliance

- Table naming: plural `tasks`, columns snake_case [Source: architecture **Naming Patterns**].
- **No** Git integration yet—that is **Epic 3**.

### Out of scope

- Assignments to interns (**2.2**).
- Intern-visible list (**2.3**).

### References

- [Source: `_bmad-output/planning-artifacts/epics.md` — Story 2.1, Epic 2]
- [Source: `_bmad-output/planning-artifacts/architecture.md` — Structure, FR7 mapping]

## Dev Agent Record

### Agent Model Used
_(filled by dev agent)_
### Debug Log References
### Completion Notes List
### File List
_(filled by dev agent on completion)_

---

**Story completion status:** `done` — Ultimate context engine analysis completed.
