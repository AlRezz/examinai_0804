# Story 2.2: Assign tasks to interns

Status: review

<!-- Ultimate context engine analysis completed - comprehensive developer guide created. -->

## Story

As a **mentor or administrator**,
I want **to assign a task to one or more interns**,
so that **each intern sees only their own assigned work later**.

## Acceptance Criteria

1. **Assign** — Given an existing task and intern user accounts (**INTERN** role),
   when an authorized mentor/admin assigns selected interns,
   then assignments **persist** and can be **listed** for that task.

2. **Mutate assignments** — Given MVP scope,
   when removing or changing assignments,
   then behavior is **defined and documented** (e.g. replace-all vs add/remove).

3. **Traceability** — Supports **FR8** foundation; aligns with privacy expectations leading to **FR25** in **2.3**.

## Tasks / Subtasks

- [x] Liquibase: `task_assignments` (or `intern_task_assignments`) with FKs to `tasks` and `users`; unique constraint where appropriate (AC: #1).
- [x] Domain services: assign, list by task, update strategy per AC #2 (AC: #1–#2).
- [x] Mentor/admin UI: pick task, multi-select interns, submit (AC: #1–#2).
- [x] Secure endpoints: same policy as task management—mentor/admin only (AC: #1).
- [x] Tests: service + web slice proving intern cannot assign (AC: #1). _(Isolation covered in `Epic2TaskAndInternIntegrationTest`.)_

## Dev Notes

### Prerequisites

- **2.1** tasks exist; **Epic 1** users with **INTERN** role available.

### Architecture compliance

- Join table naming: e.g. `task_assignments` with `task_id`, `intern_user_id` [Source: architecture **Naming Patterns**].

### Out of scope

- Intern task list UI (**2.3**).
- Submissions (**2.4**).

### References

- [Source: `_bmad-output/planning-artifacts/epics.md` — Story 2.2]

## Dev Agent Record

### Agent Model Used

Composer (Cursor agent)

### Debug Log References

### Completion Notes List

- Added `task_assignments` with unique `(task_id, intern_user_id)`. **`TaskAssignmentService.replaceAssignmentsForTask`** implements **replace-all**: POST clears previous rows for the task and re-inserts selected interns; interns must have role `intern`. UI: `/tasks/{id}/assignments` with checkboxes. `UserManagementService.listInternsOrderedByEmail()` feeds intern list.

### File List

- `src/main/resources/db/changelog/changes/003-epic2-tasks-assignments-submissions.yaml` (task_assignments section)
- `src/main/java/com/examinai/app/domain/task/TaskAssignment.java`
- `src/main/java/com/examinai/app/domain/task/TaskAssignmentRepository.java`
- `src/main/java/com/examinai/app/service/TaskAssignmentService.java`
- `src/main/java/com/examinai/app/service/UserManagementService.java`
- `src/main/java/com/examinai/app/domain/user/UserRepository.java`
- `src/main/java/com/examinai/app/web/task/TaskAssignmentController.java`
- `src/main/resources/templates/tasks/assign.html`
- `src/main/resources/templates/tasks/list.html`
- `src/test/java/com/examinai/app/web/Epic2TaskAndInternIntegrationTest.java`

---

**Story completion status:** `review` — Implementation complete; run independent code review per workflow.
