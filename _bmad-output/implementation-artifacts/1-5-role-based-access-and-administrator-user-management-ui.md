# Story 1.5: Role-based access and administrator user management UI

Status: review

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

- [x] Extend **SecurityConfig**: URL patterns or method security for `/admin/**` (or chosen prefix) restricted to `ADMIN` (or `ROLE_ADMIN`—match **1.3** authority model) (AC: #1–#3).
- [x] `UserService` (or admin application service): create/update/disable + role assignment; **transactional** writes; encode passwords via same encoder as **1.3** (AC: #1).
- [x] Thymeleaf forms: list users, create/edit, role multi-select or checkboxes; **CSRF** on POST (**NFR13**).
- [x] Validation messages user-safe (**NFR6**).
- [x] Tests: integration test proving 403 for `INTERN` (AC: #2); `@WebMvcTest` omitted here to avoid duplicate `UserDetailsService` beans with security auto-configuration while still exercising the real filter chain.

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

Auto (Cursor agent)

### Debug Log References

- Authority naming uses seeded role `administrator` → `ROLE_ADMINISTRATOR` via `UserRoleAuthorities` (Spring `hasRole("ADMINISTRATOR")`).

### Completion Notes List

- `UserManagementService` for transactional create/update with BCrypt, enable/disable, role assignment; Liquibase `002` adds `users.enabled`.
- `AdminUserController` under `web.admin` with list, create, and edit flows; Thymeleaf under `templates/admin/`.
- URL rule `/admin/**` → `hasRole("ADMINISTRATOR")`; intern denied with 403 (integration test).
- Production-oriented `server.error.include-stacktrace=never` in `application-prod.yml` for NFR6 baseline on default error handling.

### File List

- `src/main/resources/db/changelog/changes/002-user-account-enabled.yaml`
- `src/main/resources/db/changelog/db.changelog-master.yaml`
- `src/main/java/com/examinai/app/domain/user/User.java`
- `src/main/java/com/examinai/app/domain/user/UserRepository.java`
- `src/main/java/com/examinai/app/service/UserManagementService.java`
- `src/main/java/com/examinai/app/config/SecurityConfig.java` (admin URL rule — shared with 1.4)
- `src/main/java/com/examinai/app/web/admin/AdminUserController.java`
- `src/main/java/com/examinai/app/web/admin/CreateUserRequest.java`
- `src/main/java/com/examinai/app/web/admin/EditUserRequest.java`
- `src/main/resources/templates/admin/users/list.html`
- `src/main/resources/templates/admin/user-form.html`
- `src/main/resources/application-prod.yml`
- `src/test/java/com/examinai/app/web/LoginAndSecurityIntegrationTest.java` (403 + CSRF)
- `src/test/java/com/examinai/app/domain/user/UserRepositoryTest.java`
- `README.md`

## Change Log

- 2026-04-08 — Admin user management UI, `enabled` column, service layer, and security/access tests.

---

**Story completion status:** `review` — Implementation complete; ready for code review.
