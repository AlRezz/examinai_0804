# Story 1.1: Bootstrap application from Spring Initializr

Status: ready-for-dev

<!-- Ultimate context engine analysis completed - comprehensive developer guide created. -->

## Story

As a **developer**,
I want **a generated Spring Boot 3.x / Java 21 project with Thymeleaf, JPA, PostgreSQL, Liquibase, Security, Validation, Actuator, and Spring AI dependencies**,
so that **the codebase matches the architecture baseline and later stories can add domain features incrementally**.

## Acceptance Criteria

1. **Spring AI–compatible Boot** — Given the team has chosen a Boot version compatible with Spring AI, when dependencies are resolved, then the BOM/stack starts without dependency conflicts (verify against [Spring AI getting started](https://docs.spring.io/spring-ai/reference/getting-started.html) at generation time).

2. **Initializr / Maven baseline** — Given the project is generated from Spring Initializr (curl or start.spring.io) and imported as a **Maven** project, when the root is opened in the IDE, then `pom.xml` includes: **web**, **thymeleaf**, **data-jpa**, **postgresql**, **liquibase**, **security**, **validation**, **actuator**, and **spring-ai-ollama** (or team-validated Spring AI starter for the chosen inference path).

3. **Dev profile + health** — Given the `dev` Spring profile is active, when the application starts (`mvn spring-boot:run -Dspring-boot.run.profiles=dev` or equivalent), then an Actuator **health** endpoint is reachable and suitable for future Docker Compose health checks (default `/actuator/health` unless explicitly re-mapped in this story).

4. **Package root** — Given the architecture base package is `com.examinai.app`, when the codebase is inspected, then the main class lives under that package and subpackages **`config`**, **`domain`**, **`web`**, **`integration`** are not required to contain types yet (they may be omitted until later stories; do not invent domain code in this story).

5. **Prod-oriented profile** — Given **`application-prod.yml`** (or `application-pilot.yml`) exists with minimal settings, when the app runs with that profile, then a **minimal health smoke test** does not depend on developer-only tools (e.g. no requirement for local-only devtools in prod profile).

6. **Implements traceability** — This story addresses architecture **Initializr/starter** decision, **NFR2** (baseline for later TTFB work), **NFR10** (static resource pipeline can remain minimal; no heavy caching story yet).

## Tasks / Subtasks

- [ ] **Confirm Boot + Spring AI** (AC: #1)  
  - [ ] Before generating, check Spring AI compatibility matrix for chosen `bootVersion`; adjust `bootVersion` on start.spring.io if needed.

- [ ] **Generate project** (AC: #2)  
  - [ ] Use Initializr with: `type=maven-project`, `javaVersion=21`, `packageName=com.examinai.app`, `groupId=com.examinai`, `artifactId=examinai`, dependencies as listed in Dev Notes / architecture curl example.  
  - [ ] Unzip into repo; ensure `.gitignore` covers `target/`, IDE files, local env.

- [ ] **Profiles and Actuator** (AC: #3, #5)  
  - [ ] Add or verify `application.yml` with default profile or explicit `spring.profiles.active` for local dev.  
  - [ ] Add `application-dev.yml` with anything needed for local run (placeholders for DB URLs acceptable if datasource is not validated until story 1.2 — see Dev Notes).  
  - [ ] Add `application-prod.yml` with production-oriented defaults: avoid exposing full Actuator beyond health where practical; no secrets in file.

- [ ] **Verify startup** (AC: #3)  
  - [ ] Document one command to run with `dev` profile and confirm `/actuator/health` responds.

- [ ] **README slice** (AC: #2–#5)  
  - [ ] Short README: Java 21, Maven, how to run with `dev`, link to architecture doc for full stack.

## Dev Notes

### Epic cross-story context (Epic 1)

- **Epic goal:** Secure access and administration — sign-in, sessions, roles, admin user management — on top of a verified Spring Boot baseline.
- **Following stories:** 1.2 adds PostgreSQL + Liquibase; 1.3 user persistence + BCrypt; 1.4 session login/logout/CSRF; 1.5 admin UI and RBAC. **Do not** implement DB migrations, Security user details, or login pages in 1.1.

### Technical requirements (guardrails)

- **Greenfield:** There is no existing `pom.xml` in this workspace; the deliverable is the **initial commit-ready scaffold** in the repo root (architecture suggests root folder `examinai/` — align artifact name with repo layout; if the repo root *is* the project, `artifactId` may match workspace name `examinai_0804` **or** use `examinai` per architecture; **pick one** and keep `packageName=com.examinai.app` unless PM/architect updates ADR).
- **Java:** 21. **Build:** Maven with wrapper preferred after import (`mvn wrapper` if not generated).
- **Spring AI starter:** **`spring-ai-ollama`** per architecture (local inference via Ollama-compatible endpoint). Do not add ad-hoc LLM HTTP clients in this story.
- **Datasource:** Initializr may add PostgreSQL driver and JPA; story 1.2 will wire Liquibase execution and real connectivity. For 1.1, if the default generated config **requires** a live DB to start, add **minimal** `application-dev.yml` (e.g. H2) or disable auto-config only if necessary — prefer documenting "start Postgres" only if unavoidable; architecture expects real Postgres from 1.2 onward.

### Architecture compliance

- Follow [Source: `_bmad-output/planning-artifacts/architecture.md` — **Starter Template Evaluation**] for the **curl** example: `bootVersion=3.5.13.RELEASE` was documented **2026-04-08** as compatible with Initializr metadata; **re-check** `start.spring.io` before generating.
- Package layout future state: [Source: architecture **Implementation Patterns → Structure Patterns** and **Project Structure & Boundaries**] — `com.examinai.app` + `config`, `domain`, `web`, `integration.git`, `integration.ai`.
- Actuator: health for Compose; restrict non-health endpoints in prod in a later hardening pass — for 1.1, ensure **health** works and document intent [Source: architecture **Authentication & Security → Actuator**].
- **Out of scope for 1.1:** Bootstrap/jQuery WebJars, `docker-compose.yml`, `@ControllerAdvice`, domain entities, Liquibase changelog content beyond what Initializr might generate — those come in subsequent stories unless already in the generated zip.

### Library / framework requirements

| Area | Choice |
|------|--------|
| Spring Boot | 3.x line confirmed against Spring AI (example **3.5.13.RELEASE** in architecture) |
| Java | 21 |
| Starters | web, thymeleaf, data-jpa, postgresql, liquibase, security, validation, actuator, spring-ai-ollama |
| Build | Maven |

### File structure requirements

- Align with [Source: architecture **Project Structure & Boundaries**] tree: `src/main/resources/application*.yml`, future `db/changelog/`, `templates/`, `static/` — **create only** what is needed for this story (YAML + generated Java tree).
- **Tests:** `src/test/java` mirrors `com.examinai.app`; default context load test may fail until datasource is wired — use `@SpringBootTest` with appropriate test slice or exclude datasource auto-config **only if** the team chooses to enforce passing tests in 1.1 (prefer fixing config so **minimal** test passes).

### Testing requirements

- **Minimum:** Application **starts** under `dev` profile; **health** endpoint responds.
- **If** a `@SpringBootTest` is included: ensure CI/local `mvn verify` does not require Docker/Postgres unless already started — coordinate with **optional** test profile `application-test.yml` (architecture mentions Testcontainers as optional later).
- JUnit 5 via `spring-boot-starter-test`; suffix `*Test` for test classes [Source: architecture **Structure Patterns**].

### Project context reference

- No `project-context.md` found in repo; rely on **architecture.md**, **epics.md**, and **prd.md** under `_bmad-output/planning-artifacts/`.

### Latest technical information (2026)

- Re-validate **Spring Boot** and **Spring AI** versions on [https://start.spring.io/](https://start.spring.io/) and [Spring AI reference](https://docs.spring.io/spring-ai/reference/getting-started.html) before merge; architecture notes **Boot 4.0.x** as alternative after explicit compatibility review.
- Teams on Boot 3.5.x / Spring AI 1.x should track vendor EOL and security advisories; prefer current patched minors at generation time.

### References

- [Source: `_bmad-output/planning-artifacts/epics.md` — Epic 1, Story 1.1]
- [Source: `_bmad-output/planning-artifacts/architecture.md` — Starter Template Evaluation, Implementation Patterns, Project Structure]
- [Source: `_bmad-output/planning-artifacts/epics.md` — Additional Requirements (persistence, security, MPA, implementation sequence)]
- Spring AI: https://docs.spring.io/spring-ai/reference/getting-started.html

## Dev Agent Record

### Agent Model Used

_(filled by dev agent)_

### Debug Log References

### Completion Notes List

### File List

_(filled by dev agent on completion)_

---

**Story completion status:** `ready-for-dev` — Ultimate context engine analysis completed; comprehensive developer guide created.
