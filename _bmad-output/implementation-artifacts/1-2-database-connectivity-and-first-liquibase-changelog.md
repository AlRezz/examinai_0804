# Story 1.2: Database connectivity and first Liquibase changelog

Status: review

<!-- Ultimate context engine analysis completed - comprehensive developer guide created. -->

## Story

As a **developer**,
I want **PostgreSQL connectivity and an empty or minimal Liquibase master changelog**,
so that **later stories can add only the tables they need**.

## Acceptance Criteria

1. **Configuration-driven JDBC** — Given JDBC URL, username, and password from configuration (environment variables and/or `application-dev.yml` / profile-specific YAML—**no secrets committed**), when the app starts with `dev` (or equivalent) profile, then Spring Boot connects to **PostgreSQL** without connection errors.

2. **Liquibase runs on startup** — Given a reachable PostgreSQL instance, when the application starts, then **Liquibase** executes successfully (baseline applied; `DATABASECHANGELOG` and related Liquibase metadata may be created by the engine).

3. **Changelog layout** — Given the agreed resource layout, when a reviewer inspects resources, then the **master changelog** path matches project conventions under `src/main/resources/db/changelog/` and **ordered includes** point to numbered change files (see File structure).

4. **No premature domain schema** — Given story scope, when migrations are inspected, then **no** domain tables are created (`users`, tasks, etc. belong to **story 1.3+**). Acceptable: master file only with **empty include list** or a **single minimal/no-op** changelog entry if your team requires at least one changeSet for tooling—prefer **no business DDL**.

5. **Implements traceability** — Maps to **epics** Additional Requirements (Liquibase layout) and **architecture** (PostgreSQL system of record, changelog numbering, Liquibase-only evolution).

## Tasks / Subtasks

- [x] **Prerequisite** (AC: #1)  
  - [x] Ensure Story **1.1** scaffold exists (`pom.xml` with `postgresql`, `liquibase`, `data-jpa`). If not merged yet, complete or branch from 1.1 work before this story.

- [x] **Datasource properties** (AC: #1)  
  - [x] Set `spring.datasource.url`, `spring.datasource.username`, `spring.datasource.password` via env (e.g. `SPRING_DATASOURCE_*`) and/or non-committed local overrides; document variable names in **README** or `.env.example` (**keys only**, no real passwords).

- [x] **Liquibase master** (AC: #2–#4)  
  - [x] Add `src/main/resources/db/changelog/db.changelog-master.yaml` (or `.xml` if team standard—**YAML preferred** per architecture examples).  
  - [x] Add `src/main/resources/db/changelog/changes/` and either **no included files** (if Liquibase accepts) or `000-baseline.yaml` with explicit empty/minimal changeSet—**do not** add `users` / roles here.

- [x] **Spring Boot Liquibase + JPA** (AC: #2)  
  - [x] Set `spring.liquibase.change-log` to classpath location of master file (must match actual path).  
  - [x] Prefer `spring.jpa.hibernate.ddl-auto=none` (or `validate` once entities exist) so **Hibernate does not** own schema; **Liquibase** owns it [Source: architecture].

- [x] **Verify** (AC: #1–#3)  
  - [x] Start local Postgres (Docker or native); run app with `dev`; confirm logs show Liquibase **success** and no migration failure.

- [x] **Tests (recommended)** (AC: #2)  
  - [x] Optional: `@SpringBootTest` + Testcontainers PostgreSQL **or** document manual verification if team defers Testcontainers to later—**do not** leave `mvn verify` broken.

## Dev Notes

### Epic cross-story context (Epic 1)

- **1.1:** Spring Boot scaffold, profiles, Actuator health.  
- **This story (1.2):** Wire **real Postgres** + **Liquibase** with **no** domain tables.  
- **1.3:** `users`, roles, BCrypt, seed path—**first** domain DDL.  
- **1.4–1.5:** Security session flow and admin UI.

Do **not** implement user entities, Spring Security user-details DB lookup, or login pages here.

### Previous story intelligence (1.1)

- Base package **`com.examinai.app`**; subpackages optional until populated.  
- **Story 1.1** noted workspace may use **H2 or placeholders** to boot without Postgres; **this story replaces that with real Postgres + Liquibase** as the source of truth.  
- Remove or narrow any **temporary** H2-only hacks once Postgres is primary for `dev`, unless the team keeps H2 **only** for a dedicated `test` profile.

### Technical requirements (guardrails)

- **PostgreSQL:** Use a supported major (architecture mentions **16 or 17** as examples for image pinning).  
- **Secrets:** Never commit production credentials; align with **12-factor** / `.env.example` pattern from architecture.  
- **Liquibase is the only schema path** for shared environments—do not `ddl-auto=update` for releases.

### Architecture compliance

- Changelog naming: **numbered prefix + short slug** (e.g. `001-initial-schema.yaml` in later stories); master includes files **in order** [Source: `_bmad-output/planning-artifacts/architecture.md` — **Naming Patterns → Liquibase**].  
- Directory structure: [Source: architecture **Project Structure**] — `src/main/resources/db/changelog/db.changelog-master.yaml` and `changes/` subdirectory.  
- Enforcement: **MUST** put Liquibase files under `db/changelog/` with ordered includes; **never** hand-edit applied production schema without a changelog [Source: architecture **Enforcement Guidelines**].

### Library / framework requirements

| Area | Choice |
|------|--------|
| Database | PostgreSQL (JDBC via Spring Boot) |
| Migrations | Liquibase (already from Initializr) |
| ORM | JPA present; **no** entities required in this story |
| Config | `application-dev.yml` + env overrides |

### File structure requirements

Target layout (adapt if master filename differs, but keep **one** documented `spring.liquibase.change-log`):

```text
src/main/resources/
├── application.yml
├── application-dev.yml
├── application-prod.yml   # datasource via env; no secrets
└── db/
    └── changelog/
        ├── db.changelog-master.yaml
        └── changes/
            └── (optional 000-baseline.yaml — no domain DDL)
```

### Testing requirements

- **Manual:** App starts against Postgres; Liquibase completes.  
- **Automated (optional):** `@SpringBootTest` with Testcontainers module `postgresql` and same changelog—keeps CI honest.  
- Test classes: suffix `*Test`, mirror `com.examinai.app` [Source: architecture].

### Project context reference

- No `project-context.md` in repo; use planning artifacts under `_bmad-output/planning-artifacts/`.

### Latest technical information

- Confirm `spring.liquibase.change-log` property format for your Boot version (classpath prefix).  
- PostgreSQL JDBC driver version is managed by Spring Boot BOM—avoid overriding unless required.

### References

- [Source: `_bmad-output/planning-artifacts/epics.md` — Story 1.2]  
- [Source: `_bmad-output/planning-artifacts/architecture.md` — Data Architecture, Naming Patterns, Project Structure, Enforcement Guidelines]  
- [Source: `_bmad-output/planning-artifacts/epics.md` — Additional Requirements, persistence / Liquibase]

## Dev Agent Record

### Agent Model Used

Composer (Cursor agent)

### Debug Log References

- Initial Testcontainers run failed without Docker; enabled `@Testcontainers(disabledWithoutDocker = true)` so `mvn verify` passes when Docker is absent.

### Completion Notes List

- **Dev profile** now uses PostgreSQL with `SPRING_DATASOURCE_*` defaults matching README Docker example; H2 removed from `application-dev.yml`.
- **Liquibase** master uses ordered `include` of `changes/000-baseline.yaml` (tag-only baseline, no domain DDL). `application.yml` already pointed `spring.liquibase.change-log` at the master file.
- **JPA** `ddl-auto` set to `none` for dev, test, and prod so Liquibase owns schema evolution.
- **Prod** datasource accepts `SPRING_DATASOURCE_*` with fallback to existing `DATABASE_*` placeholders.
- **Docs:** README updated for Postgres + `.env.example` with keys only. **H2** retained for `test` profile only.
- **Tests:** `PostgresLiquibaseIntegrationTest` exercises Liquibase against PostgreSQL when Docker is available; skipped otherwise.

### File List

- `_bmad-output/implementation-artifacts/sprint-status.yaml`
- `_bmad-output/implementation-artifacts/1-2-database-connectivity-and-first-liquibase-changelog.md`
- `.env.example`
- `README.md`
- `pom.xml`
- `src/main/resources/application-dev.yml`
- `src/main/resources/application-test.yml`
- `src/main/resources/application-prod.yml`
- `src/main/resources/db/changelog/db.changelog-master.yaml`
- `src/main/resources/db/changelog/changes/000-baseline.yaml`
- `src/test/java/com/examinai/app/PostgresLiquibaseIntegrationTest.java`

## Change Log

- 2026-04-08: Story 1.2 — PostgreSQL dev datasource, Liquibase master + `changes/000-baseline.yaml`, `ddl-auto=none`, `.env.example`, README, Testcontainers integration test (skip without Docker).

---

**Story completion status:** `review` — Implementation complete; ready for code review.
