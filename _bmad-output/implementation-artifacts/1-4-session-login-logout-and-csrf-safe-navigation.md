# Story 1.4: Session login, logout, and CSRF-safe navigation

Status: ready-for-dev

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

- [ ] Implement `SecurityFilterChain`: form login, logout, session management; **secure / HttpOnly** cookie settings for non-`dev` per architecture (AC: #1–#3, #5).
- [ ] Implement `UserDetailsService` (or equivalent) loading from **1.3** persistence (AC: #1).
- [ ] Thymeleaf login template + minimal landing after login (AC: #1, #4); use `th:action` with CSRF (AC: #5).
- [ ] Protect sample secured route to demonstrate redirect (**FR3**).
- [ ] Tests: `@WebMvcTest` or integration test for login flow smoke; avoid brittle full E2E unless team standard (AC: #1–#3).

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
_(filled by dev agent)_
### Debug Log References
### Completion Notes List
### File List
_(filled by dev agent on completion)_

---

**Story completion status:** `ready-for-dev` — Ultimate context engine analysis completed.
