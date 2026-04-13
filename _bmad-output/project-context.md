---
project_name: examinai_0804
user_name: Alex
date: '2026-04-08'
sections_completed:
  - technology_stack
  - critical_implementation_rules
discovery_notes: >-
  Brownfield Spring Boot app; Git integration is RestClient-only in
  com.examinai.app.integration.git.GitSourceClient. Align doc changes with
  docs/planning-artifacts/architecture.md and README Git section.
codebase_status: implemented
---

# Project Context for AI Agents

_This file contains critical rules and patterns that AI agents must follow when implementing code in this project. Focus on unobvious details that agents might otherwise miss._

---

## Technology Stack & Versions

| Layer | Choice | Notes |
|--------|--------|--------|
| Language | Java **21+** | PRD / architecture |
| Framework | **Spring Boot** | **Confirm** `bootVersion` against [Spring AI compatibility](https://docs.spring.io/spring-ai/reference/getting-started.html); architecture used **3.5.13.RELEASE** as an example—re-check [start.spring.io](https://start.spring.io/) before generating |
| Web UI | **Spring MVC** + **Thymeleaf** | MPA only for MVP; no SPA requirement |
| Persistence | **PostgreSQL**, **Spring Data JPA** / Hibernate, **Liquibase** | Pin DB **major** deliberately (e.g. 16 or 17) |
| Security | **Spring Security** | Session auth, roles, CSRF on browser mutations |
| AI | **Spring AI** (e.g. **`spring-ai-ollama`** for local inference) | Target model **deepseek-r1:8b** in dedicated container; timeouts, retries, degraded UX |
| Frontend assets | **Bootstrap** **5** | **WebJars** + shared fragment `fragments/head-bootstrap` (story **8.1**); **jQuery** per PRD where used |
| Deploy | **Docker Compose** | Three services: app, PostgreSQL, LLM runtime |
| Testing | **spring-boot-starter-test** (JUnit 5, Mockito) | Co-located `*Test` classes |

When the Maven/Gradle project exists, **copy exact dependency versions from the generated BOM** into updates to this section.

---

## Critical Implementation Rules

### Naming

- **Database:** `snake_case`, plural tables (`users`, `tasks`). FK columns `{table_singular}_id`. Indexes `idx_{table}_{columns}`. Liquibase files under `db/changelog/changes/` with numbered prefix + slug; ordered includes from `db.changelog-master.yaml`.
- **URLs (MVC):** lowercase paths, plural nouns (`/tasks`, `/submissions/{id}/review`). Spring `@{/path}` in Thymeleaf consistently.
- **Java:** `PascalCase` types; `*Controller`, `*Service`, `*Repository`, `*Form` / `*Command` for stereotypes. Packages feature-first under `com.examinai.app`: `domain`, `web`, `config`, `integration.git`, `integration.ai`.

### Structure & boundaries

- **Controllers:** bind input, delegate to **services**, return view + model—**no** heavy business logic or direct repository use for publish / AI draft / cross-aggregate flows.
- **Services:** own **transactions** for publish review, persist AI draft, Git snapshot metadata, and related audit rows.
- **Git HTTP:** only in **`integration.git`**; use **`RestClient` only** (see `GitClientConfig`, `GitSourceClient`). Do **not** add **WebClient** for Git provider calls.
- **LLM:** only via **`integration.ai`** / Spring AI façade—**no** raw HTTP from controllers.
- **Templates:** `templates/` by area (`tasks/`, `review/`, `fragments/`, `layouts/`). **Static** under `static/css`, `static/js`.

### Web behavior

- **Writes:** **redirect-after-POST** + `RedirectAttributes` flash for success/error—not JSON envelopes for MVP unless a story adds AJAX.
- **Dates/times:** `java.time` in **UTC**; format in UI with Thymeleaf `#temporals` or shared formatters.
- **Degraded LLM:** reuse **one** convention for banner/flags (e.g. model attribute `degradedInference` or flash namespace `app.degraded.*`) once introduced.

### Errors & logging

- **`@ControllerAdvice`** (or consistent split) maps domain and integration failures to **safe** user messages. **No** stack traces or raw provider messages to users in **prod**.
- Typed integration errors (e.g. `GitProviderException`, `InferenceUnavailableException`) translated before UI.
- **Structured logging** in prod; **never** log secrets, raw tokens, or full Git/LLM payloads—ids, outcome, duration only.

### Security & privacy

- **Secrets** from env / Compose—**not** plaintext in DB or code. **Minimize** code sent to LLM per policy; record **model id/version**, timestamp, optional prompt hash for audit.
- **Authorization:** enforce intern isolation (outcomes), mentor vs admin vs coordinator read-only paths per PRD roles.

### Frontend

- **Server-side** model + flash; **jQuery** for small UX only—avoid ad hoc `window.APP` globals unless one module owns them.
- Target **WCAG 2.1 AA** on login, queue, review, publish, intern feedback flows.

### Testing

- Tests in `src/test/java` mirroring main packages; suffix **`Test`** (JUnit 5).
- Optional `src/test/resources/application-test.yml` (Testcontainers / test profile) when introduced.

### Session / events

- **No** domain-event bus in MVP. If using `ApplicationEvent`, same-JVM only; name `NounPastTenseEvent`.
- **No** Redis/caching in MVP unless a story adds it after measurement.

### Anti-patterns (do not do)

- Mixed `userId` / `user_id` in schema without explicit `@Column` / global naming strategy.
- Returning **500** with **Git** or **LLM** supplier message text to end users.
- Hand-editing production schema without a Liquibase change.
- **Mentor publish** or **AI draft** persistence implemented as controller → repository skips.

### Git provider (`integration.git`)

- **Config:** `examinai.git.base-url` ← **`GIT_PROVIDER_BASE_URL`**, token ← **`GIT_PROVIDER_TOKEN`** (never in DB/UI).
- **HTTP:** **`RestClient`** with `Accept: application/vnd.github+json` and `Authorization: Bearer …` when token is set.
- **Coordinates:** `owner/repo`, commit **`ref`**, and **path scope** (required repo-relative path—**no** default such as `README.md`).
- **Calls (order):**
  1. `GET {base}/repos/{owner}/{repo}/commits/{ref}` — commit JSON + `files[]`.
  2. For the path scope, if a matching `files[]` entry exists: use **`patch`**, else GET **`raw_url`**, else GET **`contents_url`** and decode Contents JSON (`type: file`, base64 `content`).
  3. If no matching `files[]` row: `GET {base}/repos/{owner}/{repo}/contents/{path}?ref={ref}`.
- **Errors:** `GitProviderException` + `GitFailureKind`; map to safe mentor strings (`GitRetrievalUiMessage`), never raw provider bodies.

---

## When the code scaffold lands

1. Update **Technology Stack** table with exact **Spring Boot**, **Spring AI**, driver, and WebJar versions from `pom.xml` or `build.gradle`.
2. If **ID strategy** (UUID vs long) is decided, state it here and use it consistently in URLs and entities.
3. Keep this file **lean**; large rewrites belong in `architecture.md`—sync any **new** convention here in the same PR.

---

_Generated from architecture implementation patterns. For BMAD interactive refinement (Advanced Elicitation / Party Mode per category), say **continue project context** and specify which section to deepen._
