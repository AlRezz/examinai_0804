# Story 7.4: PostgreSQL credentials and Compose alignment (fix `role "root" does not exist`)

Status: done

## Story

As an **operator**,
I want **database credentials for the app and Postgres container to come from one clear, documented contract**,
so that **`docker compose up` and local runs never attempt to connect as a non-existent PostgreSQL role (e.g. `root`)**.

## Problem statement

Observed in logs:

```text
FATAL:  role "root" does not exist
```

PostgreSQL has no `root` superuser like some other databases. This error usually means a **client** connected (or tried to) with username `root`, or credentials were **empty / inconsistent** with how the data directory was first initialized (`POSTGRES_USER` vs JDBC user).

Likely contributors to investigate:

- **Missing or empty `POSTGRES_*` / `SPRING_DATASOURCE_*`** in `.env`, while an existing volume already initialized the cluster with a different user.
- **Compose interpolation**: nested defaults (e.g. `SPRING_DATASOURCE_USERNAME` falling through to another var) behaving differently across Compose versions or leaving an empty value where the JDBC stack then picks an unsafe default.
- **Operational confusion**: running `psql` inside a container as UID 0 and relying on peer/OS defaults instead of `-U` matching `POSTGRES_USER`.

## Acceptance Criteria

1. **Single source of truth** — `docker-compose.yml` uses **explicit, consistent** env wiring so the **app JDBC user** always matches the **database role** created by the `db` service for greenfield installs (same variable names or documented pass-through; no silent mismatch).

2. **Safe defaults for pilot** — If `.env` is absent or incomplete, **documented defaults** (e.g. same placeholder user/password/db name as `.env.example`) apply so Postgres initializes and the app connects **without** attempting role `root` or an empty username.

3. **Documentation** — `.env.example` and `docs/runbook-pilot.md` include a **Troubleshooting** bullet: `FATAL: role "root" does not exist` — verify `POSTGRES_USER` matches `SPRING_DATASOURCE_USERNAME`, recreate volume if cluster was initialized under a different user, and never rely on implicit DB usernames.

4. **Traceability** — Extends **7.2** (FR32), **7.3** (NFR12 operator docs), architecture deployment/env hygiene.

## Tasks / Subtasks

- [x] Reproduce or reason through the failure mode (empty vs wrong user vs volume mismatch); confirm fix with `docker compose up` + app health.
- [x] Adjust `docker-compose.yml` (and only if needed `application-*.yml`) so JDBC username/password cannot diverge from `db` service env for the bundled stack.
- [x] Update `.env.example` and `docs/runbook-pilot.md` troubleshooting.
- [x] Optional: add a one-line log-friendly validation in dev (fail fast with clear message if datasource username is blank) — only if consistent with project style — **skipped** (Compose + `:-examinai` defaults remove empty JDBC user; no extra Java hook).

## Dev Notes

### Prerequisites

- **7.2** Compose topology; **7.3** runbook and `.env.example` baseline.

### References

- [Source: `_bmad-output/planning-artifacts/epics.md` — Epic 7]
- PostgreSQL image: role creation follows `POSTGRES_*` on first init of the data directory.

## Dev Agent Record

### Agent Model Used

Composer (Cursor agent)

### Debug Log References

- `docker compose config` — resolved `SPRING_DATASOURCE_*` and `POSTGRES_*` to `examinai` when defaults apply.

### Completion Notes List

- **`docker-compose.yml`**: `POSTGRES_USER` / `PASSWORD` / `DB` default to **`examinai`** when unset; **`SPRING_PROFILES_ACTIVE`** defaults to **`dev`**. App JDBC URL uses **`${POSTGRES_DB:-examinai}`** in the default path; **`SPRING_DATASOURCE_USERNAME`** / **`PASSWORD`** use **`${SPRING_DATASOURCE_*:-${POSTGRES_*:-examinai}}`** so optional `SPRING_*` overrides still work, and empty `POSTGRES_*` no longer yields a blank JDBC user.
- **`.env.example`**: Notes single-source **`POSTGRES_*`** and that Postgres has no `root` role.
- **`docs/runbook-pilot.md`**: Troubleshooting for **`FATAL: role "root" does not exist`** (align users, volume reset, `psql -U`).

### File List

- `docker-compose.yml`
- `.env.example`
- `docs/runbook-pilot.md`

## Change Log

| Date       | Change        |
| ---------- | ------------- |
| 2026-04-10 | Story created |
| 2026-04-10 | Implemented: Compose defaults, docs, runbook troubleshooting |
