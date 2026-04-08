---
stepsCompleted:
  - 1
  - 2
  - 3
  - 4
  - 5
  - 6
  - 7
  - 8
inputDocuments:
  - _bmad-output/planning-artifacts/prd.md
workflowType: architecture
project_name: examinai_0804
user_name: Alex
date: '2026-04-08'
lastStep: 8
status: complete
completedAt: '2026-04-08'
---

# Architecture Decision Document

_This document builds collaboratively through step-by-step discovery. Sections are appended as we work through each architectural decision together._

## Project Context Analysis

### Requirements Overview

**Functional Requirements:**

The PRD defines **32** capabilities, architecturally clustering as: **authentication and session identity** (FR1–FR4); **admin-managed users and roles** (FR5–FR6); **tasks and intern submissions with VCS coordinates** (FR7–FR9); **Git/source retrieval with diagnostics and retry** (FR10–FR12); **mentor queue and review workspace including rubric scores, free-text feedback, and publish as official outcome** (FR13–FR17); **AI draft request/persistence and invocation metadata, with mentor able to complete without AI** (FR18–FR21); **intern-facing published outcomes, draft vs published distinction, lifecycle status, and privacy between interns** (FR22–FR25); **coordinator/auditor read-only case record and publication provenance/version history** (FR26–FR28); **detectable degraded inference with mentor-only publish path** (FR29–FR30); **admin/deploy-time Git configuration boundaries and split topology support** (FR31–FR32). Together these imply a **single domain model** spanning **users, tasks, submissions, retrieved artifacts, AI drafts, published reviews, and audit metadata**, with **strict separation** between **assistive model output** and **authoritative mentor decisions**.

**Non-Functional Requirements:**

A dedicated PRD **NFR section was omitted** by stakeholder choice; still, **architecturally binding** expectations appear elsewhere: **WCAG 2.1 AA**-oriented goals on **login, queue, review, publish, and intern feedback**; **server TTFB** target **< 500 ms** P95 (pilot hardware, excluding cold starts); **Git fetch** **< 60 s** P95 for typical MVP scope; **bounded LLM timeouts/retries** and **non-blocking** UX patterns; **password hashing**, **secure/HttpOnly** session cookies in non-dev; **no stack traces** to users in production; **zero** accidental **secret export** to the LLM in pilot; **Compose** health and **documented** degraded behavior. **Compliance deep-dives** are explicitly out of PRD scope; **baseline security, privacy discipline, and safe UX** remain in scope.

**Scale & Complexity:**

- **Primary domain:** Full-stack **web application** (MPA: Spring MVC + Thymeleaf + progressive enhancement), with **integrations** to **Git provider APIs** and a **local LLM** via **Spring AI**.
- **Complexity level:** **Medium** (PRD classification: edtech / internship programs; mentor–intern trust and labeling; operational tri-container deployment).
- **Estimated architectural components (conceptual):** **Web/UI layer** (mentor queue, review, intern feedback, admin/seed surfaces); **application services** (authz, task/submission lifecycle, publishing); **integration adapters** (Git retrieval, LLM client); **persistence** (relational model + migrations); **observability/config** (health, env/secrets, model/version tracking)—roughly **5–7** major building blocks for planning purposes, refinable in later steps.

### Technical Constraints & Dependencies

- **Stack commitments (PRD):** **Java 21+**, **Spring** ecosystem, **PostgreSQL**, **Liquibase**, **Hibernate**, **Spring Security**, **Thymeleaf**, **Bootstrap**, **jQuery**, **Docker Compose** with **separate images** for app, database, and LLM (target model **deepseek-r1:8b** in a **dedicated container**).
- **MVP shape:** Server-rendered **MPA** (no SPA requirement); **sessions** and **CSRF** per Spring defaults; **no CSV import**; roster/tasks via **in-app** or **seed** data.
- **External dependencies:** **Version-control provider API** with org-managed credentials; **local inference endpoint** with explicit **failure modes**.

### Cross-Cutting Concerns Identified

- **Authorization and multi-tenant privacy of outcomes** (interns must not see others’ grades; roles for mentor/intern/admin/coordinator paths).
- **Auditability** of **retrieval, AI invocation, and publication** (who/when, model identity, optional prompt hash, draft vs published).
- **Degraded operation** when **Git** or **LLM** fails (clear UX, mentor-only path, no silent wrong finality).
- **Safety and trust UX** (draft labeling, mentor attribution, consistency under concurrent refresh scenarios per PRD journeys).
- **Secret hygiene** (Compose/env secrets, no plaintext tokens in DB, minimal payload to LLM).
- **Accessibility and performance** on the flows called out in the PRD for WCAG and latency targets.

## Starter Template Evaluation

### Primary Technology Domain

**Java full-stack web (MPA)** — server-rendered UI with **Spring MVC + Thymeleaf**, relational persistence, and **Spring AI** integration to a **local inference** runtime, as defined in the PRD.

### Starter Options Considered

| Option | Assessment |
|--------|-------------|
| **Spring Initializr** (`start.spring.io`) | **Selected** — official generator; aligns with PRD stack; exposes current Boot versions and **Spring AI** starters (e.g. `spring-ai-ollama`, `spring-ai-deepseek`). |
| Third-party Spring boilerplates | Deferred — add opinionated structure only if the team outgrows a clean Initializr baseline; not required for MVP. |
| **JHipster** / heavy full-stack generators | **Out of scope for MVP** — SPA-centric features and extra complexity are not PRD-aligned. |

### Selected Starter: Spring Initializr

**Rationale for Selection:** The PRD already mandates **Spring Boot**, **Java 21+**, **Thymeleaf**, **JPA + Liquibase + PostgreSQL**, **Spring Security**, and **Spring AI**. Initializr is the maintained, standard way to lock those dependencies and **Boot** version explicitly; Compose and WebJars are layered on afterward.

**Initialization command:**

Use the `bootVersion` your team confirms against the **Spring AI** compatibility matrix (see [Spring AI reference — getting started](https://docs.spring.io/spring-ai/reference/getting-started.html)). Example with **Spring Boot 3.5.13** and **Ollama** Spring AI module (common for a local `deepseek-r1:8b` served compatibly with Ollama):

```bash
curl -G "https://start.spring.io/starter.zip" \
  --data-urlencode "type=maven-project" \
  --data-urlencode "language=java" \
  --data-urlencode "bootVersion=3.5.13.RELEASE" \
  --data-urlencode "baseDir=examinai" \
  --data-urlencode "groupId=com.examinai" \
  --data-urlencode "artifactId=examinai" \
  --data-urlencode "name=examinai" \
  --data-urlencode "packageName=com.examinai.app" \
  --data-urlencode "javaVersion=21" \
  --data-urlencode "dependencies=web,thymeleaf,data-jpa,postgresql,liquibase,security,validation,actuator,spring-ai-ollama" \
  -o examinai.zip
```

Unzip, import as Maven project. Regenerate from [start.spring.io](https://start.spring.io/) if you prefer **Gradle** or **Spring Boot 4.0.x** after compatibility checks.

**Architectural Decisions Provided by Starter:**

**Language & runtime:** Java **21**, Maven (or Gradle if chosen), Spring Boot BOM manages dependency versions.

**Styling solution:** Not included — add **Bootstrap** and **jQuery** via **WebJars** or static resources per PRD.

**Build tooling:** Maven/Gradle wrapper, Spring Boot plugin, executable JAR packaging.

**Testing framework:** `spring-boot-starter-test` (JUnit, Mockito, etc.) included by default with most Initializr sets; confirm when generating.

**Code organization:** Standard **single-module** layout (`src/main/java`, `resources`, `templates` for Thymeleaf); domain packages remain a team choice in later steps.

**Development experience:** Dev tooling optional (`devtools`); **Actuator** for **health** endpoints in Compose.

**Note:** Running this generator and committing the scaffold should be the **first implementation story**; Docker Compose for app/DB/LLM is a follow-on.

**Versions verified:** Initializr metadata fetched **2026-04-08** lists `bootVersion` **3.5.13.RELEASE** and **4.0.5.RELEASE** (default **4.0.5.RELEASE**); [Spring announcement — Spring Boot 3.5.13](https://spring.io/blog/2026/03/26/spring-boot-3-5-13-available-now). Re-check `start.spring.io` before generating.

## Core Architectural Decisions

### Decision Priority Analysis

**Critical Decisions (Block Implementation):**

- **Persistence:** PostgreSQL as **system of record**; **Liquibase** for schema migrations; **Spring Data JPA / Hibernate** for domain persistence — aligns with PRD and Initializr selection.
- **Security baseline:** **Spring Security** with **database-backed** users, **session** (cookie) authentication, **role-based** access for intern / mentor / administrator (and coordinator path for FR26); **CSRF** on browser mutations — PRD.
- **Application style:** **MPA** — Spring MVC controllers, **Thymeleaf** views, **form posts** / redirects for writes; no MVP requirement for a separate SPA or public REST product API.
- **Integrations:** **Git** retrieval via **server-side HTTP client** to provider APIs using **secrets from deployment config** (not plaintext in DB); **Spring AI** to **local inference** (Initializr: **`spring-ai-ollama`** or team-validated alternative) with **timeouts, retries, and degraded behavior** per PRD.
- **Deployment topology:** **Docker Compose** with **three replaceable** services: application, PostgreSQL, LLM runtime — PRD FR32.

**Important Decisions (Shape Architecture):**

- **Data validation:** **Jakarta Bean Validation** on binding models/command objects; enforce invariants at **service** boundary; DB **constraints** for integrity (FKs, uniqueness where needed).
- **Caching:** **None in MVP** unless profiling proves need; **revisit** for queue dashboards or heavy read paths post-pilot.
- **Transaction boundaries:** **Write operations** (publish review, persist AI draft, Git snapshot metadata) use explicit **transactional** application services to keep **audit rows** consistent.
- **API documentation:** **No** OpenAPI-first public API in MVP; document **internal** integration touchpoints (Git, LLM) in **developer/runbook** docs.
- **Error handling:** Global **`@ControllerAdvice`** + human-friendly **Thymeleaf** error pages in production; map Git/LLM failures to **actionable** UI states (per journeys).
- **Observability:** **Spring Boot Actuator** **health** for Compose; **structured logging** (e.g. JSON in prod profile); avoid exposing full actuator in production without **network restriction** or **separate management** access.
- **Frontend architecture:** **Server-rendered** pages; **Bootstrap** layout + **jQuery** enhancements via **WebJars** or static resource pipeline; **no** client-side SPA router or global JSON state store in MVP.

**Deferred Decisions (Post-MVP):**

- **Redis / distributed sessions**, horizontal **replication**, **Kubernetes** (beyond Compose), **SSO**, **multi-tenant** isolation hardening — Growth/Vision in PRD.
- **Separate BFF or public REST** for integrations — only if product expands beyond MPA.

### Data Architecture

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Database | **PostgreSQL** | PRD; image tag pinned to a **major** (e.g. 17 or 16), minors upgraded deliberately |
| Migrations | **Liquibase** | PRD + starter |
| ORM / model | **JPA** entities + repositories; **lazy/eager** tuned per aggregate | Standard Spring stack; keep **submission/review** aggregates cohesive |
| Identifiers | **UUID or long** per team convention — **decide in implementation** with preference for **DB-generated** or **UUID** for audit-friendly IDs | Document in ADR when first entity lands |
| Caching | **Omitted MVP** | Simplicity; measure before adding Caffeine/Redis |
| Large/binary | **No** raw Git blobs in DB if avoidable — store **metadata + normalized text scope** per domain design (exact storage TBD in implementation stories) | PRD hints at snapshot metadata and hashing |

### Authentication & Security

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Authentication | **Session login** (Spring Security) | PRD; no SSO in MVP |
| Passwords | **BCrypt** (or Spring-recommended **delegating** encoder) | Baseline credential protection |
| Authorization | **Method + URL** security; **role → feature** matrix aligned to FR4 | Mentors vs interns vs admin vs coordinator read-only |
| Secrets | **Compose secrets / env** for Git tokens and LLM endpoints; **never** log payloads containing secrets | PRD success/risk |
| LLM boundary | **Minimize** code sent; **policy** for scrubbing; record **model id/version + timestamp (+ optional prompt hash)** | FR20, audit |
| Actuator | **Health** enabled; other endpoints **restricted** in `prod` | Reduces attack surface |

### API & Communication Patterns

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Primary client integration | **HTML** over HTTP from browser to Spring MVC | PRD MPA |
| Provider calls | **Spring WebClient** (reactive stack) or **RestClient** for **Git HTTP** — pick one stack-wide in implementation | Non-blocking-friendly for timeouts |
| DTO / error mapping | Provider errors → **domain** failures with **safe** user messages (FR11) | Security + UX |
| Inter-service comm | **App → Postgres** JDBC; **App → LLM** via Spring AI client; **no** extra sync services in MVP | Matches Compose |

### Frontend Architecture

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Rendering | **Thymeleaf** + fragments/layouts | PRD |
| CSS / JS | **Bootstrap** + **jQuery** | PRD; WebJars/static |
| State | **Server session** + request scope; **no** SPA global store | Simpler alignment with mentor-publish flows |
| Accessibility | Target **WCAG 2.1 AA** on critical flows | PRD |
| Performance | Server **TTFB** goals per PRD; optimize queries before front-end caching | |

### Infrastructure & Deployment

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Hosting model | **On-prem / laptop pilot** via **Docker Compose** | PRD |
| CI/CD | **Deferred** to team preference; minimum: **build JAR + image**, **liquibase update** on startup | Document in implementation |
| Environments | `dev` / `prod` (or `pilot`) profiles; **secure cookies** and **no devtools** in prod | PRD |
| Config | **Externalized configuration** (env); **12-factor** alignment for secrets | Compose-friendly |

### Decision Impact Analysis

**Implementation sequence (suggested):**

1. Initializr scaffold + core config profiles + Actuator health  
2. Liquibase baseline + **user/role** tables + Spring Security  
3. Domain tables for **tasks, submissions, reviews, AI metadata**  
4. Git retrieval service + failure UX  
5. Spring AI integration + degraded mode  
6. Thymeleaf flows: intern + mentor + coordinator read-only path  
7. Compose files + documented env/secrets  

**Cross-component dependencies:**

- **Published review** depends on **transactional** writes linking **submission version**, **mentor identity**, and **optional AI draft** rows.  
- **LLM unavailability** must not block **mentor-only publish** (FR29–FR30).  
- **Git failure** must not corrupt **prior** submission snapshots (re-fetch/correct coordinates per FR12).

## Implementation Patterns & Consistency Rules

### Pattern Categories Defined

**Critical conflict points addressed:** naming (DB, URLs, Java), package/feature layout, HTTP form/redirect conventions, error and logging shapes, Thymeleaf/CSS placement, and external HTTP client choice — roughly **12** recurring decision surfaces where agents could otherwise disagree.

### Naming Patterns

**Database naming conventions:**

- **Tables:** `snake_case`, **plural** where natural (`users`, `tasks`, `submissions`). Join tables: `user_roles`, `task_assignments` (examples).
- **Columns:** `snake_case` (`user_id`, `created_at`). **FK columns** named `{referenced_table_singular}_id` (e.g. `task_id`).
- **Indexes:** `idx_{table}_{columns}` (e.g. `idx_submissions_task_id`).
- **Liquibase:** changelog files under `db/changelog/` with **numbered** prefix + short slug (e.g. `001-initial-schema.yaml`); include in `db.changelog-master.yaml` in order.

**API / URL naming (browser-facing MVC):**

- **Paths:** **lowercase**, **kebab-free** segments with **hyphens avoided** — prefer **nouns** plural (`/tasks`, `/submissions/{id}/review`). Use **Spring `@{/path}`** in Thymeleaf consistently.
- **Path variables:** `{id}` in `@GetMapping("/submissions/{id}")` — use **UUID or long** consistently once ID strategy is fixed in implementation story #1.
- **Form field names:** match command object **JavaBean** properties (`camelCase`); Thymeleaf `th:field` binds by property name.

**Java code naming:**

- **Classes:** `PascalCase`. **Spring stereotypes:** `*Controller`, `*Service`, `*Repository`, `*Form` / `*Command` for binding types.
- **Methods / variables:** `camelCase`. **Constants:** `UPPER_SNAKE`.
- **Packages (feature-first):** root `com.examinai.app` + **`domain`**, **`web`**, **`config`**, **`integration.git`**, **`integration.ai`** (adjust base to match Initializr `packageName`). Avoid dumping all entities in one flat package without sub-features once model grows.

### Structure Patterns

**Project organization:**

- **Tests:** **Co-located** by default: `src/test/java` mirrors `main` package tree; test class suffix `*Test` (JUnit 5).
- **Features:** group by **bounded context** folders (`task`, `submission`, `review`, `user`) under `domain` and `web` rather than strictly layering only by technical type at the top level.
- **Static assets:** `src/main/resources/static/` (CSS JS images); **WebJars** for Bootstrap and jQuery in Maven deps.
- **Templates:** `src/main/resources/templates/` with subfolders mirroring areas (`tasks/`, `review/`, `fragments/`).
- **Config:** `config` package for security, actuator restrictions, WebClient beans; profile-specific YAML: `application-dev.yml`, `application-prod.yml`.

### Format Patterns

**API response formats (browser MVC):**

- **Primary:** **redirect-after-POST** with **Spring `RedirectAttributes`** for flash **success/error** messages — not full-page JSON.
- **No** standard JSON envelope for MVP public routes unless a story explicitly adds AJAX; if added later: **direct body** or single wrapper — **pick one** in that story and document here.

**Data exchange:**

- **Java ↔ DB:** JPA maps snake column naming via `@Column` where defaults differ; prefer **naming strategy** configuration once globally rather than per-field chaos.
- **Dates/times:** **`java.time.Instant` or `OffsetDateTime`** stored in **UTC**; format in UI with Thymeleaf **#temporals** (or explicit `DateTimeFormatter` in config bean).
- **Booleans:** `boolean` / `BIT`; **no** numeric `1/0` in Java APIs.

**Logging fields (structured JSON in prod):**

- Include **correlation id** (MDC) if introduced; always **never** log raw tokens, passwords, or full Git/LLM payloads — log **ids + outcome + duration**.

### Communication Patterns

**Event system patterns:**

- **No** mandated domain-events bus in MVP. Optional **Spring `ApplicationEvent`** only for **same-JVM** side effects; name events `NounPastTenseEvent` (e.g. `ReviewPublishedEvent`). **Do not** introduce Kafka/etc. without architecture update.

**State management (UI):**

- **Server-side** flash + model attributes; **jQuery** for minor UX only — **no** shared global mutable JS state object naming like `window.APP` unless one module owns it.

### Process Patterns

**Error handling:**

- **One** `@ControllerAdvice` (or split by base package **by convention**) for **MVC** exceptions; map domain failures to **4xx** user-safe messages. **Do not** expose stack traces in prod (align with PRD).
- **Provider errors:** translate to a **small** set of **typed** integration exceptions (`GitProviderException`, `InferenceUnavailableException`) handled in advice.

**Loading / degraded paths:**

- **Banner + flag** pattern for LLM down (PRD); use **consistent** model attribute name e.g. `degradedInference` **or** flash key namespace `app.degraded.*` — agents must **reuse** the same keys once introduced in the first story that implements the banner.

### Enforcement Guidelines

**All AI agents MUST:**

- Follow **DB snake_case plural** and **Java camelCase** rules above for **new** schema and code.
- Put **Liquibase** changes only in **`db/changelog/`** with ordered includes; never hand-edit applied production schema without a changelog.
- Use **one** HTTP client style for Git (**WebClient** or **RestClient**, not both) — first integration story locks it; later stories reference this doc.
- Route **mentor publish** and **AI draft persistence** through **transactional services** (no controller-direct repository writes for those flows).

**Pattern enforcement:**

- **PR / review:** check package placement and naming against this section.
- **Updates:** change this architecture doc when intentionally adopting a new convention; do not invent one-off patterns in a single story without updating here.

### Pattern Examples

**Good examples:**

- Table `reviews`, columns `mentor_user_id`, `submission_id`, `published_at`.
- Controller `ReviewController` + service `ReviewService` + `ReviewRepository`.
- Changelog `002-add-review-published-at.yaml` adding `published_at TIMESTAMPTZ NOT NULL` with rollback where feasible.

**Anti-patterns:**

- Mixed `userId` / `user_id` column naming in same schema without explicit `@Column`.
- Returning **500** with exception message text from **Git provider** to interns.
- **RestClient** and **WebClient** both used for different Git calls in the same codebase without documented exception.

## Project Structure & Boundaries

### Complete Project Directory Structure

Repository root **`examinai/`** (Maven artifact may match). Base package **`com.examinai.app`** (align with Initializr).

```text
examinai/
├── pom.xml
├── README.md
├── .gitignore
├── .env.example
├── Dockerfile
├── docker-compose.yml
├── src/main/java/com/examinai/app/
│   ├── ExaminaiApplication.java
│   ├── config/
│   │   ├── SecurityConfig.java
│   │   ├── WebMvcConfig.java
│   │   └── GitClientConfig.java          # WebClient or RestClient bean(s)
│   ├── domain/
│   │   ├── user/
│   │   │   ├── User.java
│   │   │   ├── UserRepository.java
│   │   │   └── UserService.java
│   │   ├── task/
│   │   ├── submission/
│   │   └── review/
│   ├── integration/
│   │   ├── git/
│   │   │   └── GitSourceClient.java      # provider HTTP; no secrets in code
│   │   └── ai/
│   │       └── AssessmentDraftClient.java # Spring AI facade; timeouts here
│   └── web/
│       ├── HomeController.java
│       ├── task/
│       ├── submission/
│       ├── review/
│       ├── admin/
│       └── advice/
│           └── GlobalExceptionHandler.java
├── src/main/resources/
│   ├── application.yml
│   ├── application-dev.yml
│   ├── application-prod.yml
│   ├── static/
│   │   ├── css/
│   │   └── js/
│   ├── templates/
│   │   ├── layouts/
│   │   ├── fragments/
│   │   ├── tasks/
│   │   ├── submissions/
│   │   └── review/
│   └── db/
│       └── changelog/
│           ├── db.changelog-master.yaml
│           └── changes/
│               ├── 001-initial-schema.yaml
│               └── ...
├── src/test/java/com/examinai/app/
│   └── ...                                 # mirrors main packages
└── src/test/resources/
    └── application-test.yml                # optional: Testcontainers / H2 profile
```

_Add files as stories introduce them; names above are illustrative anchors, not an exhaustive checklist._

### Architectural Boundaries

**API boundaries:**

- **Browser ↔ app:** HTTP to Spring MVC only; **authenticated** routes behind Spring Security; **CSRF** on mutating forms.
- **App ↔ PostgreSQL:** JDBC via Spring Data JPA; **no** direct SQL in controllers.
- **App ↔ Git provider:** only through **`integration.git`**; credentials from **environment**; errors mapped before UI.
- **App ↔ LLM runtime:** only through **`integration.ai`** and Spring AI config; **no** ad-hoc HTTP from controllers.

**Component boundaries:**

- **Controllers** parse input, delegate to **services**, choose view + model; **no** heavy business rules in controllers.
- **Domain services** own transactions for publish/draft/retrieval workflows; **repositories** stay persistence-shaped.

**Service boundaries:**

- **`UserService`** (accounts, roles) vs **`ReviewService`** (publish, scores) vs **`SubmissionService`** (lifecycle, coordinates) — avoid a single “god” service; split by aggregate/story if needed.

**Data boundaries:**

- **Liquibase** is the **only** schema evolution path for shared environments.
- **AI draft rows** vs **published review rows** remain separate tables or clearly distinguished columns — no overwriting mentor verdict with model text.

### Requirements to Structure Mapping

**FR category → primary location**

| FR group | Primary packages / resources |
|----------|------------------------------|
| FR1–FR4 Identity | `config/SecurityConfig`, `domain/user`, `web` login templates |
| FR5–FR6 Admin | `web/admin`, `UserService` |
| FR7–FR9 Tasks / submissions | `domain/task`, `domain/submission`, `web/task`, `web/submission` |
| FR10–FR12 Git | `integration/git`, Liquibase tables for retrieval metadata |
| FR13–FR17 Mentor workspace | `web/review`, `domain/review`, templates `review/` |
| FR18–FR21 AI assistance | `integration/ai`, `domain/review` (draft persistence) |
| FR22–FR25 Intern UX | `web/submission` (intern), templates with draft vs published |
| FR26–FR28 Audit | read-only controllers/services + shared queries; optional `web/audit` |
| FR29–FR30 Degraded | `integration/ai` (health/timeout), shared banner fragment |
| FR31–FR32 Config / topology | `application-*.yml`, `docker-compose.yml`, env docs |

**Cross-cutting**

| Concern | Location |
|---------|----------|
| Security | `config/SecurityConfig`, method security on services where appropriate |
| Errors / flash | `web/advice/GlobalExceptionHandler`, layout fragments |
| Logging / MDC | `config` + logging pattern in `application-prod.yml` |

### Integration Points

**Internal:** Controllers → application **services** → **repositories**; optional **domain events** same JVM only.

**External:** Git REST/API client; Spring AI to **Ollama** (or chosen) endpoint; PostgreSQL TCP.

**Data flow (simplified):** Intern submits coordinates → **submission** saved → **git** fetch persists artifact metadata → mentor opens review → **AI draft** optional → mentor **publish** → intern sees **published** row only.

### File Organization Patterns

**Configuration:** `application.yml` + profile overlays; secrets **never** committed — `.env.example` documents keys only.

**Source:** feature-first under `domain/*` and `web/*`; integrations isolated under `integration/*`.

**Tests:** mirror packages; use `@WebMvcTest`, `@DataJpaTest`, integration tests with Testcontainers **if/when** adopted.

**Assets:** WebJars + `static/`; reusable Thymeleaf pieces under `templates/fragments/`.

### Development Workflow Integration

**Local dev:** `mvn spring-boot:run` with `dev` profile; optional local Postgres + LLM via Compose.

**Build:** `mvn -Pprod package` → executable JAR; **Dockerfile** multi-stage optional.

**Deploy:** `docker compose up` — **app**, **db**, **llm** services; app reads JDBC and AI URLs from env.

## Architecture Validation Results

### Coherence Validation

**Decision compatibility:** Spring **MPA**, **session security**, **PostgreSQL + Liquibase + JPA**, **Git + Spring AI** sidecars, and **Compose** triad are consistent. **WebClient** or **RestClient** (one only, per patterns) is mutually exclusive by pattern. **No SPA** assumption matches **Thymeleaf + jQuery** PRD.

**Pattern consistency:** Naming (DB snake, Java camel, URL plural nouns), Liquibase layout, controller/service split, and integration package boundaries reinforce the core decisions.

**Structure alignment:** Directory tree supports **domain** vs **integration** vs **web** separation and FR-to-folder mapping.

### Requirements Coverage Validation

**Epic / feature coverage:** No epic artifact in `inputDocuments`; validation used **PRD FR categories** and **user journeys**.

**Functional requirements coverage:** **FR1–FR32** have a home in **Core Architectural Decisions** and/or **Project Structure & Boundaries** mapping table.

**Non-functional coverage:** Performance, accessibility, security baseline, degraded LLM, audit metadata, and secret handling are addressed via **Project Context**, **Core Decisions**, and **Patterns** (plus PRD success criteria referenced implicitly).

### Implementation Readiness Validation

**Decision completeness:** Critical stack choices documented; **version pins** for Boot/Postgres images are **verify-at-generate-time** where upstream moves fast.

**Structure completeness:** Concrete **examinai/** tree and FR→location map provided; file names are **illustrative** — new types appear as stories land.

**Pattern completeness:** High-risk conflict zones (DB naming, HTTP client, transactional publish path, Liquibase-only schema) have explicit MUST rules and anti-patterns.

### Gap Analysis Results

| Priority | Gap | Suggested handling |
|----------|-----|-------------------|
| Important | **Spring Boot major/minor** vs **Spring AI** compatibility | Confirm on Initializr + Spring AI docs before first merge of scaffold |
| Important | **PK strategy** (UUID vs long) | First entity story + small ADR |
| Nice | **Provider-specific Git** API | First `integration/git` story |
| Nice | **Binary/text storage** for retrieved sources | Domain story; keep DB thin per architecture |
| Nice | **Testcontainers** adoption | Optional; `application-test.yml` placeholder exists |

### Validation Issues Addressed

No **critical** contradictions found. Gaps above are **tracked** for implementation stories rather than blocking this document.

### Architecture Completeness Checklist

**Requirements analysis**

- [x] Project context analyzed  
- [x] Scale / complexity assessed  
- [x] Technical constraints identified  
- [x] Cross-cutting concerns mapped  

**Architectural decisions**

- [x] Critical decisions documented (with verify-at-gen for fast-moving versions)  
- [x] Stack specified for MVP  
- [x] Integration patterns defined  
- [x] Performance / accessibility / security considerations referenced  

**Implementation patterns**

- [x] Naming conventions  
- [x] Structure patterns  
- [x] Communication / process patterns  
- [x] Examples and anti-patterns  

**Project structure**

- [x] Directory structure defined  
- [x] Boundaries documented  
- [x] Integration points mapped  
- [x] FR → structure mapping  

### Architecture Readiness Assessment

**Overall status:** **Ready for implementation** (with Boot/AI compatibility confirmation as first technical task).

**Confidence level:** **Medium–high**

**Key strengths:** PRD-aligned stack; strong **mentor-vs-AI** and **integration** boundaries; enforceable consistency rules for agents.

**Areas for future enhancement:** Multi-module split, API versioning, full **OpenAPI**, advanced observability — post-MVP per PRD.

### Implementation Handoff

**AI agent guidelines**

- Treat this file as authoritative for stack, boundaries, and patterns.  
- Do not introduce a second HTTP client style for Git, second schema migration path, or controller-direct publish writes.

**First implementation priority**

- Run **Spring Initializr** (see **Starter Template Evaluation**), confirm **Spring AI** dependency set for chosen **Boot** version, commit scaffold, then Liquibase + Security baseline.
