# Story 1.3: User identity persistence and password hashing

Status: done

<!-- Ultimate context engine analysis completed - comprehensive developer guide created. -->

## Story

As a **developer**,
I want **user records stored with hashed passwords and role linkage**,
so that **Spring Security can authenticate against the database**.

## Acceptance Criteria

1. **Liquibase schema** — Given Liquibase changes for `users` and role assignment (dedicated `roles` / `user_roles` or equivalent join) per architecture naming (**snake_case**, plural tables),
   when migrations apply,
   then the schema is valid and **no plaintext passwords** are stored in DDL or seed data committed to the repo.

2. **Password hashing** — Given new users created via application or documented seed path,
   when passwords are persisted,
   then they are encoded with **BCrypt** or Spring’s **DelegatingPasswordEncoder** pattern [Source: architecture **Authentication & Security**].

3. **Role linkage** — Given the chosen model (authorities table, `user_roles`, or enum column—**pick one** and document),
   when a user row exists,
   then at least one **role** can be associated for Spring Security mapping (**intern**, **mentor**, **administrator**, reserve **coordinator** if needed for FR26 later).

4. **Local pilot bootstrap** — Given local development,
   when an operator follows README-only instructions (no secrets in repo),
   then they can obtain an **initial admin** account for testing (migration with placeholder hash + doc to reset, or SQL snippet in docs—not production secrets).

5. **Traceability** — Implements **FR1** foundation, **FR5–FR6** data model prep, **NFR5**; aligns with architecture persistence and naming patterns.

## Tasks / Subtasks

- [x] Add numbered changelog under `db/changelog/changes/` (e.g. `001-users-and-roles.yaml`) included from master (AC: #1).
- [x] Add JPA entities + repositories in `com.examinai.app.domain.user` (or feature subpackages) — **minimal** surface for persistence; Security integration comes in 1.4 (AC: #2–#3).
- [x] Configure password encoder bean in `config` (AC: #2).
- [x] Document admin bootstrap in README (AC: #4).
- [x] Tests: repository or `@DataJpaTest` for entities; migration applies on test profile if available (AC: #1).

## Dev Notes

### Previous story (1.2)

- Postgres + Liquibase baseline must be in place; this story adds **first domain tables**.
- Set `spring.jpa.hibernate.ddl-auto=none` (or validate)—**only Liquibase** evolves schema.

### Epic context

- **1.4** adds session login, **UserDetailsService**, and login templates. **Do not** need full login UI here unless you need it to verify persistence manually.

### Architecture compliance

- Tables: `users`, `roles` or `user_roles` per [Source: `_bmad-output/planning-artifacts/architecture.md` — **Naming Patterns**].
- **Identifiers:** decide **UUID vs long** here and document; stick to it for later aggregates.
- **Never** log passwords or tokens (NFR12 pattern).

### Role model (story decision)

- **Normalized roles:** table `roles` with stable lowercase codes `intern`, `mentor`, `administrator`, `coordinator`; **many-to-many** via `user_roles`. Spring `GrantedAuthority` naming (e.g. `ROLE_*`) can be applied in **1.4** when `UserDetailsService` is implemented.

### Out of scope

- HTTP login/logout, CSRF browser flows (**1.4**).
- Admin CRUD UI (**1.5**).

### References

- [Source: `_bmad-output/planning-artifacts/epics.md` — Story 1.3]
- [Source: `_bmad-output/planning-artifacts/architecture.md` — Data Architecture, Authentication & Security, Enforcement]

## Dev Agent Record

### Agent Model Used

Auto (Cursor agent)

### Debug Log References

- H2 `DATABASE_TO_LOWER=TRUE` + shared `jdbc:h2:mem:examinai_test` caused Liquibase to attempt recreating `DATABASECHANGELOG` when a second Spring test context started; fixed with `examinai_test_${random.uuid}` and `DB_CLOSE_DELAY=0` in `application-test.yml`.

### Completion Notes List

- Liquibase `001-users-and-roles.yaml`: `users`, `roles`, `user_roles`, FKs, seeds for four roles + bootstrap admin (BCrypt hash only in DB).
- JPA `User` / `Role` with UUID PKs; `UserRepository` / `RoleRepository`; `PasswordEncoderConfig` exposes `BCryptPasswordEncoder`.
- README documents bootstrap admin email and temporary password; root `application.yml` sets `ddl-auto: none`.
- `UserRepositoryTest` (`@DataJpaTest`) validates seeds and BCrypt persistence; `PostgresLiquibaseIntegrationTest` asserts admin row when Docker is available.

### File List

- `src/main/resources/db/changelog/db.changelog-master.yaml`
- `src/main/resources/db/changelog/changes/001-users-and-roles.yaml`
- `src/main/resources/application.yml`
- `src/main/resources/application-test.yml`
- `src/main/java/com/examinai/app/config/PasswordEncoderConfig.java`
- `src/main/java/com/examinai/app/domain/user/User.java`
- `src/main/java/com/examinai/app/domain/user/Role.java`
- `src/main/java/com/examinai/app/domain/user/UserRepository.java`
- `src/main/java/com/examinai/app/domain/user/RoleRepository.java`
- `src/test/java/com/examinai/app/domain/user/UserRepositoryTest.java`
- `src/test/java/com/examinai/app/PostgresLiquibaseIntegrationTest.java`
- `README.md`

### Change Log

- 2026-04-08: Story 1.3 implemented — users/roles schema, BCrypt encoder, JPA layer, README bootstrap, tests; story marked **review**.

---

**Story completion status:** `done` — Marked done; aligns with `sprint-status.yaml`.
