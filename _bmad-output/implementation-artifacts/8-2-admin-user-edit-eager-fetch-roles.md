# Story 8.2: Admin user edit — eager fetch roles (fix LazyInitializationException)

Status: done

## Story

As an **administrator**,
I want **the user edit screen to load without server errors**,
so that **I can change roles and enabled state** after opening **GET `/admin/users/{id}/edit`**.

## Problem

`LazyInitializationException` on `User.roles`: the controller called `user.getRoles()` after `UserManagementService.getUserById` returned. The service method’s transaction ended, so Hibernate could no longer initialize the lazy collection.

## Acceptance Criteria

1. **GET `/admin/users/{id}/edit`** loads the form and current role checkboxes without exception.
2. **Fix** loads `roles` inside the persistence boundary used for lookup (e.g. `JOIN FETCH` / dedicated repository query), not `@Transactional` on the web layer.
3. **Regression test** covers `findWithRolesById` (or equivalent) initializing roles for seeded admin.

## Implementation

- **`UserRepository`**: `findWithRolesById` uses **`@EntityGraph(attributePaths = "roles")`** (reliable fetch with Spring Data; avoids edge cases with `Optional` + `JOIN FETCH`).
- **`UserManagementService`**: **`loadAdminEditForm(id)`** builds **`EditUserRequest` inside the transaction** (materializes role name list before session closes). Required because **`spring.jpa.open-in-view: false`** — returning a **`User`** to the controller and calling **`getRoles()`** there can still fail after the service transaction ends.
- **List query**: `findAllWithRolesOrderedByEmail` / `findAllWithRoleName` use **`@EntityGraph`** so **`admin/users` list** Thymeleaf `u.roles` iteration does not lazy-load after the tx.
- **`AdminUserEditModel`**: record carrying **`userId`**, **`email`**, **`editRequest`** from **`loadAdminEditForm`**.

## File List

- `src/main/java/com/examinai/app/domain/user/UserRepository.java`
- `src/main/java/com/examinai/app/service/UserManagementService.java`
- `src/main/java/com/examinai/app/service/AdminUserEditModel.java`
- `src/main/java/com/examinai/app/web/admin/AdminUserController.java`
- `src/test/java/com/examinai/app/domain/user/UserRepositoryTest.java`

## Change Log

| Date       | Change |
| ---------- | ------ |
| 2026-04-10 | Story created and implemented |
| 2026-04-10 | Marked **done**; tracking aligned with `sprint-status.yaml` (Epic 8) |

---

**Story completion status:** `done` — delivered (admin edit form loads roles inside persistence boundary; `open-in-view: false` compatible).
