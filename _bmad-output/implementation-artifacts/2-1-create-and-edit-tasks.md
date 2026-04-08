# Story 2.1: Create and edit tasks

Status: review

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

- [x] Liquibase: `tasks` table (snake_case columns, `due_at` or similar) + changelog include (AC: #2).
- [x] Entity `Task`, repository, `TaskService` in `domain.task` (AC: #2).
- [x] `TaskController` under `web/task` (or `web/tasks`): list, new, edit, form POST + redirect [Source: architecture **Format Patterns**] (AC: #1–#3).
- [x] Thymeleaf templates under `templates/tasks/` (AC: #2–#3).
- [x] Secure routes with Spring Security expressions matching mentor/admin (AC: #1).
- [x] Tests: service + `@WebMvcTest` or slice tests (AC: #1–#3). _(Covered via `Epic2TaskAndInternIntegrationTest` + security tests.)_

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

Composer (Cursor agent)

### Debug Log References

### Completion Notes List

- Implemented `tasks` table (`title`, `description`, `due_date`, audit columns), `Task` entity, `TaskService`, and `TaskController` with Thymeleaf list/form at `/tasks`. Routes secured with `hasAnyRole('MENTOR', 'ADMINISTRATOR')`. Interns receive 403 on `/tasks/**`.

### File List

- `src/main/resources/db/changelog/changes/003-epic2-tasks-assignments-submissions.yaml` (tasks DDL section)
- `src/main/resources/db/changelog/db.changelog-master.yaml`
- `src/main/java/com/examinai/app/domain/task/Task.java`
- `src/main/java/com/examinai/app/domain/task/TaskRepository.java`
- `src/main/java/com/examinai/app/service/TaskService.java`
- `src/main/java/com/examinai/app/web/task/TaskForm.java`
- `src/main/java/com/examinai/app/web/task/TaskController.java`
- `src/main/resources/templates/tasks/list.html`
- `src/main/resources/templates/tasks/form.html`
- `src/main/java/com/examinai/app/config/SecurityConfig.java`
- `src/main/java/com/examinai/app/security/RoleBasedAuthenticationSuccessHandler.java`
- `src/main/resources/templates/home.html`
- `src/test/java/com/examinai/app/web/Epic2TaskAndInternIntegrationTest.java`
- `src/test/java/com/examinai/app/web/LoginAndSecurityIntegrationTest.java`

---

**Story completion status:** `review` — Implementation complete; run independent code review per workflow.
