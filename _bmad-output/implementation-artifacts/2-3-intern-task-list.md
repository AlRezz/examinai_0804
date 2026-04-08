# Story 2.3: Intern task list

Status: review

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

- [x] Query service: tasks for `currentUser.id` via assignment join—**no** broad task leak (AC: #1–#2).
- [x] `InternTaskController` (or under `web/task`) + Thymeleaf list template (AC: #1).
- [x] Security: role `INTERN` (and optionally self-only resource rules) (AC: #1–#2).
- [x] Tests: two interns, assignments differ; each sees only own rows (AC: #2).

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

Composer (Cursor agent)

### Debug Log References

### Completion Notes List

- `InternTaskService.listAssignedTasksForIntern` uses `TaskAssignmentRepository.findByInternIdWithTask`. `InternTaskController` at `/intern/tasks` (list) and `/intern/tasks/{taskId}` (detail scaffold for **2.4**). Security: `/intern/**` → `hasRole('INTERN')`. Mentors receive 403 on intern routes. Unassigned task detail returns **404** to avoid leaking task existence.

### File List

- `src/main/java/com/examinai/app/service/InternTaskService.java`
- `src/main/java/com/examinai/app/web/intern/InternTaskController.java`
- `src/main/resources/templates/intern/tasks/list.html`
- `src/main/resources/templates/intern/tasks/detail.html` (shared with 2.4)
- `src/main/java/com/examinai/app/config/SecurityConfig.java`
- `src/main/java/com/examinai/app/security/RoleBasedAuthenticationSuccessHandler.java`
- `src/main/resources/templates/home.html`
- `src/test/java/com/examinai/app/web/Epic2TaskAndInternIntegrationTest.java`
- `src/test/java/com/examinai/app/web/LoginAndSecurityIntegrationTest.java`

---

**Story completion status:** `review` — Implementation complete; run independent code review per workflow.
