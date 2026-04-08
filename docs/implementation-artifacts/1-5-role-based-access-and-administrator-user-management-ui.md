# Story 1.5: Role-based access and administrator user management UI

Status: ready-for-dev

<!-- Ultimate context engine analysis completed - comprehensive developer guide created. -->

## Story

As an **administrator**,
I want **to create, disable, and update accounts and assign roles**,
so that **interns, mentors, and coordinators exist with correct permissions**.

## Acceptance Criteria

1. **Admin screens** — Given a user with **administrator** role,
   when they open user management routes,
   then they can **create** a user, **disable/update** account state, and **assign roles** (**FR5**, **FR6**).

2. **Non-admin denied** — Given a non-admin user,
   when they hit admin routes,
   then they receive **403** (or equivalent) without data leaks (**FR4**).

3. **Intern isolation on errors** — Given an intern account,
   when they attempt admin actions,
   then access is denied and error responses do not expose stack traces or sensitive internals in **prod** profile (**FR4**, **NFR6**).

4. **Traceability** — **FR4**, **FR5**, **FR6**.

## Tasks / Subtasks

- [ ] Extend **SecurityConfig**: URL patterns or method security for `/admin/**` (or chosen prefix) restricted to `ADMIN` (or `ROLE_ADMIN`—match **1.3** authority model) (AC: #1–#3).
- [ ] `UserService` (or admin application service): create/update/disable + role assignment; **transactional** writes; encode passwords via same encoder as **1.3** (AC: #1).
- [ ] Thymeleaf forms: list users, create/edit, role multi-select or checkboxes; **CSRF** on POST (**NFR13**).
- [ ] Validation messages user-safe (**NFR6**).
- [ ] Tests: `@WebMvcTest` with mock user roles; integration test proving 403 for `INTERN` (AC: #2).

## Dev Notes

### Prerequisites

- **1.3–1.4** completed: persisted users, login, session.

### Architecture compliance

- Controllers thin; delegate to services [Source: architecture **Architectural Boundaries**].
- Naming: `web/admin/` controllers, templates under `templates/admin/` [Source: architecture **Project Structure**].

### UX

- Admin flows should remain usable; WCAG push can be lighter than login but avoid obviously broken keyboard traps.

### References

- [Source: `_bmad-output/planning-artifacts/epics.md` — Story 1.5]
- [Source: `_bmad-output/planning-artifacts/architecture.md` — FR mapping FR5–FR6]

## Dev Agent Record

### Agent Model Used
_(filled by dev agent)_
### Debug Log References
### Completion Notes List
### File List
_(filled by dev agent on completion)_

---

**Story completion status:** `ready-for-dev` — Ultimate context engine analysis completed.
