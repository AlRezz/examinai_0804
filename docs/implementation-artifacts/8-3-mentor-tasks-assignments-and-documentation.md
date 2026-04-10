# Story 8.3: Mentor program tasks — documentation and Git commit retrieval

Status: done

## Story

As a **mentor**,
I want **to create tasks and assign interns with the same `/tasks` flows as an administrator**,
so that **program work is not blocked waiting for an admin when security already allows mentor access** (FR7, Epic 2).

As an **operator or mentor**,
I want **source fetch to align with GitHub’s documented “Get a commit” REST API**,
so that **coordinates and troubleshooting match `GET /repos/{owner}/{repo}/commits/{ref}`** (story extension, 2026-04-10).

## Context

**`/tasks/**`** was already secured with **`hasAnyRole("MENTOR", "ADMINISTRATOR")`**; controllers did not restrict task CRUD or assignments to administrators only. Product documentation and in-app copy still read as administrator-centric for “create task / assign interns,” which misled operators and mentors.

**Git integration (follow-up):** `GitSourceClient` uses **`GET /repos/{owner}/{repo}/commits/{ref}`** ([Commits REST API](https://docs.github.com/en/rest/commits/commits?apiVersion=2026-03-10)), then resolves file text via **`files[].patch`**, else **`raw_url`**, else **`contents_url`**, else **`GET …/contents/{path}?ref=`**. **Path scope is required** (no default file). Planning docs: **`README.md`**, **`docs/planning-artifacts/architecture.md`**, **`docs/planning-artifacts/prd.md`**, **`docs/project-context.md`**.

## Acceptance Criteria

1. **README** — *User flows (by role)* states that **mentors and administrators** create tasks and assignments; **user provisioning** stays **`/admin/users`**, administrator-only; suggested end-to-end order reflects mentor-or-admin task steps.
2. **Pilot runbook** — Documents **`/tasks`** for mentors and administrators, separation from **`/admin/users`**, and extends the smoke path with a tasks/assignments step.
3. **SecurityConfig** — Inline comment ties **`/tasks/**`** to program tasks and mentor submission workspace (traceability to FR7 / Epic 2).
4. **Thymeleaf** — **Program tasks** list and **Assign interns** explain mentor+admin usage; empty intern list shows **User management** link for administrators and plain guidance for mentors (no 403 link for non-admins).
5. **Git** — **`GitSourceClient`** (**`RestClient`**) implements commits + **patch** / **raw_url** / **contents_url** / **contents** fallback; required path scope; safe UI mapping; **README**, runbook, and planning artifacts describe behavior.

## Implementation

- **`SecurityConfig`**: Comment above **`/tasks/**`** matcher (no rule change).
- **`README.md`**: User flows; Git section documents **Get a commit** and path scope vs **`files`**.
- **`docs/runbook-pilot.md`**: Program tasks section; smoke path; Git **`NOT_FOUND`** triage row.
- **`tasks/list.html`**, **`tasks/assign.html`**: Mentor + administrator copy.
- **`GitSourceClient`**: **`GET …/commits/{ref}`**; resolution order **patch** → **raw_url** → **contents_url** → repository **contents**; **`httpGetBody`** for follow-up GETs.
- **`GitSourceClientTest`**: Commits URL, **raw_url**, contents fallback when path not in **`files[]`**, blank path, auth errors.
- **`GitRetrievalUiMessage`**: **`NOT_FOUND`** copy includes path-not-in-commit case.
- **`tasks/fragments/git-retrieval.html`**, **`tasks/submission-detail.html`**, **`intern/tasks/detail.html`**: Coordinate help aligned with commits API.

## File List

- `README.md`
- `docs/planning-artifacts/architecture.md`
- `docs/planning-artifacts/prd.md`
- `docs/project-context.md`
- `docs/runbook-pilot.md`
- `src/main/java/com/examinai/app/config/SecurityConfig.java`
- `src/main/java/com/examinai/app/integration/git/GitSourceClient.java`
- `src/main/java/com/examinai/app/web/task/GitRetrievalUiMessage.java`
- `src/main/resources/templates/tasks/list.html`
- `src/main/resources/templates/tasks/assign.html`
- `src/main/resources/templates/tasks/fragments/git-retrieval.html`
- `src/main/resources/templates/tasks/submission-detail.html`
- `src/main/resources/templates/intern/tasks/detail.html`
- `src/test/java/com/examinai/app/integration/git/GitSourceClientTest.java`

## Change Log

| Date       | Change |
| ---------- | ------ |
| 2026-04-10 | Story created; docs, SecurityConfig comment, task templates aligned to mentor + administrator program tasks |
| 2026-04-10 | **Git:** `GitSourceClient` uses **Get a commit** (`/commits/{ref}`); README, runbook, UI hints, `GitRetrievalUiMessage`, tests |
| 2026-04-10 | **Git:** Required **path scope**; body from **patch** → **raw_url** → **contents_url** → repository **contents** fallback |
| 2026-04-10 | **Done:** story closed; **architecture** / **PRD** / **project-context** synced with Git API sequence; **`sprint-status.yaml`** Epic 8 **done** |

---

**Story completion status:** `done` — mentor/admin task flows documented; Git retrieval matches implemented **`GitSourceClient`** and operator docs.
