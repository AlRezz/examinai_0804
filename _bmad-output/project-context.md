---
project_name: examinai_0804
user_name: Alex
date: '2026-04-08'
sections_completed:
  - technology_stack
  - critical_implementation_rules
discovery_notes: >-
  Spring Boot application implemented per _bmad-output/planning-artifacts/architecture.md.
  Pin Spring Boot / Spring AI versions per BOM when upgrading dependencies.
codebase_status: implementation_complete
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
| Frontend assets | **Bootstrap**, **jQuery** | Via **WebJars** or `src/main/resources/static/` |
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
- **Git HTTP:** only in **`integration.git`**; **one** client style project-wide (**WebClient** _or_ **RestClient**—lock in first integration story, never both ad hoc).
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

---

## When the code scaffold lands

1. Update **Technology Stack** table with exact **Spring Boot**, **Spring AI**, driver, and WebJar versions from `pom.xml` or `build.gradle`.
2. If **ID strategy** (UUID vs long) is decided, state it here and use it consistently in URLs and entities.
3. Keep this file **lean**; large rewrites belong in `architecture.md`—sync any **new** convention here in the same PR.

---

_Generated from architecture implementation patterns. For BMAD interactive refinement (Advanced Elicitation / Party Mode per category), say **continue project context** and specify which section to deepen._
