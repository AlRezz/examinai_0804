# Story 1.4: Session login, logout, and CSRF-safe navigation

Status: review

<!-- Ultimate context engine analysis completed - comprehensive developer guide created. -->

## Story

As a **user**,
I want **to sign in and sign out with a server-side session**,
so that **my actions run under one authenticated identity inside the app**.

## Acceptance Criteria

1. **Login success** — Given a user exists with a valid BCryped password (**1.3**),
   when they submit the login form,
   then they are authenticated and redirected to a **role-appropriate home** (placeholder route acceptable).

2. **Logout** — Given an authenticated session,
   when logout is invoked,
   then the session is cleared and subsequent protected requests are denied (**FR2**).

3. **Anonymous access** — Given an unauthenticated client,
   when they open protected URLs,
   then they are redirected to login and cannot perform mutating actions (**FR3**).

4. **Login accessibility** — Given the login page,
   when used with keyboard,
   then primary fields have **visible labels**, submit is reachable without mouse (**UX-DR1**, **NFR1** baseline).

5. **CSRF** — Given a mutating form secured by Spring Security defaults,
   when CSRF is enabled,
   then POSTs include a valid token and forged posts fail (**NFR13**).

6. **Traceability** — **FR1**, **FR2**, **FR3**, **NFR5** (session cookies non-dev), **NFR13**.

## Tasks / Subtasks

- [x] Implement `SecurityFilterChain`: form login, logout, session management; **secure / HttpOnly** cookie settings for non-`dev` per architecture (AC: #1–#3, #5).
- [x] Implement `UserDetailsService` (or equivalent) loading from **1.3** persistence (AC: #1).
- [x] Thymeleaf login template + minimal landing after login (AC: #1, #4); use `th:action` with CSRF (AC: #5).
- [x] Protect sample secured route to demonstrate redirect (**FR3**).
- [x] Tests: `@WebMvcTest` or integration test for login flow smoke; avoid brittle full E2E unless team standard (AC: #1–#3).

## Dev Notes

### Prerequisites

- **1.3** users/roles persisted and encoder compatible with stored hashes.

### Architecture compliance

- **MPA**: form POST + redirect for writes [Source: architecture **Application style**].
- **Errors:** user-safe pages in prod—no stack traces (**NFR6**); login errors generic enough not to enumerate users.
- Placeholder **GlobalExceptionHandler** not required here unless you introduce custom auth exceptions.

### Out of scope

- Administrator user-management screens (**1.5**).
- Fine-grained method security matrix across domain features—minimal URL rules OK; **1.5** hardens admin.

### References

- [Source: `_bmad-output/planning-artifacts/epics.md` — Story 1.4]
- [Source: `_bmad-output/planning-artifacts/architecture.md` — Authentication & Security, Frontend, Error handling]

## Dev Agent Record

### Agent Model Used

Auto (Cursor agent)

### Debug Log References

- Resolved Hibernate lazy init on detached `Role` by maintaining only the owning side on `User.roles` for `addRole` / `replaceRoles`.

### Completion Notes List

- Form login, DB-backed `UserDetailsService`, role-based post-login redirect (administrators → `/admin/users`, others → `/home`), logout POST with CSRF, `/app/secure` as protected sample, Thymeleaf templates with labels and CSRF-aware forms.
- Session cookies: `HttpOnly` + `secure=false` in `dev`/`test`, `secure=true` in `prod` (`application-*.yml`).
- Integration tests in `LoginAndSecurityIntegrationTest` (login redirects, anonymous redirect, logout, intern 403 on `/admin/**`, CSRF rejection on admin POST without token).

### File List

- `src/main/java/com/examinai/app/config/SecurityConfig.java`
- `src/main/java/com/examinai/app/security/DatabaseUserDetailsService.java`
- `src/main/java/com/examinai/app/security/RoleBasedAuthenticationSuccessHandler.java`
- `src/main/java/com/examinai/app/security/UserRoleAuthorities.java`
- `src/main/java/com/examinai/app/domain/user/User.java`
- `src/main/java/com/examinai/app/web/IndexController.java`
- `src/main/java/com/examinai/app/web/LoginController.java`
- `src/main/java/com/examinai/app/web/HomeController.java`
- `src/main/java/com/examinai/app/web/AppSecureController.java`
- `src/main/resources/templates/index.html`
- `src/main/resources/templates/login.html`
- `src/main/resources/templates/home.html`
- `src/main/resources/templates/app/secure.html`
- `src/main/resources/application-dev.yml`
- `src/main/resources/application-prod.yml`
- `src/main/resources/application-test.yml`
- `src/main/resources/application.yml`
- `src/test/java/com/examinai/app/web/LoginAndSecurityIntegrationTest.java`
- `README.md` (sign-in pointer)

## Change Log

- 2026-04-08 — Implemented session login/logout, CSRF defaults, cookie flags by profile, and security integration tests.

---

**Story completion status:** `review` — Implementation complete; ready for code review.
