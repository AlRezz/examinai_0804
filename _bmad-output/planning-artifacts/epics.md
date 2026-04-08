---
stepsCompleted:
  - step-01-requirements-extracted
  - step-02-epic-list-approved
  - step-03-stories-complete
  - step-04-validation-complete
inputDocuments:
  - _bmad-output/planning-artifacts/prd.md
  - _bmad-output/planning-artifacts/architecture.md
workflowType: epics-and-stories
project_name: examinai_0804
user_name: Alex
date: '2026-04-08'
uxDesignDocument: none-found
status: complete
completedAt: '2026-04-08'
---

# examinai_0804 - Epic Breakdown

## Overview

This document provides the complete epic and story breakdown for examinai_0804, decomposing the requirements from the PRD, UX Design if it exists, and Architecture requirements into implementable stories.

**Inputs used:** PRD (`prd.md`) and Architecture (`architecture.md`). No separate UX specification file was found under `planning-artifacts`; UX-oriented requirements were taken from the PRD **Web application specific requirements** and **Domain-specific requirements**.

## Implementation status (sprint tracking)

Authoritative per-story keys: [`_bmad-output/implementation-artifacts/sprint-status.yaml`](../implementation-artifacts/sprint-status.yaml). Story markdown lives under `_bmad-output/implementation-artifacts/`.

| Epic | Engineering status | Notes |
|------|-------------------|--------|
| **1** — Secure access and administration | **Done** | Stories **1.1–1.5** **done** (bootstrap, Liquibase, users/roles, login/CSRF, admin UI). |
| **2** — Program tasks and intern submissions | **Done** | Stories **2.1–2.4** **done** (tasks, assignments, intern list, submissions). |
| **3** — Trusted source retrieval from Git | **Done** | Stories **3.1–3.3** **done** (Git config/hygiene, fetch client, diagnostics UX). |
| **4** — Mentor review workspace and official outcomes | **Done** | Stories **4.1–4.5** **done** (queue, detail + source, draft rubric, publish w/ provenance, outcome history). |
| **5** — AI-assisted drafts and resilient inference | In progress | Stories **5.1–5.4** **ready-for-dev**. |
| **6** — Intern transparency and privacy-safe feedback | In progress | Stories **6.1–6.4** **ready-for-dev**. |
| **7** — Audit visibility and pilot-ready deployment | In progress | Stories **7.1–7.3** **ready-for-dev**. |

## Requirements Inventory

### Functional Requirements

FR1: A user can sign in using organization-provided credentials.

FR2: A user can sign out and end an authenticated session.

FR3: The product can enforce that each action runs under exactly one authenticated identity.

FR4: The product can restrict features by role (e.g. intern, mentor, administrator).

FR5: An administrator can create, disable, and update user accounts.

FR6: An administrator can assign and change user roles.

FR7: An authorized user (mentor or administrator, per policy) can create and edit tasks with instructions and due expectations.

FR8: An intern can view tasks assigned to them.

FR9: An intern can attach a submission to a task by supplying version-control coordinates (e.g. repository and revision).

FR10: The system can retrieve source content for a submission from an external version-control provider using organization-configured credentials.

FR11: The system can record retrieval success, failure, and diagnostic details without exposing secrets to end users.

FR12: An authorized user can re-trigger retrieval or correct coordinates when retrieval fails (within policy).

FR13: A mentor can view a list of submissions awaiting or in review.

FR14: A mentor can open a submission and inspect retrieved source in context of the task.

FR15: A mentor can enter or adjust structured scores for quality, readability, and correctness.

FR16: A mentor can enter or adjust free-text feedback independent of model-generated text.

FR17: A mentor can publish a review so it becomes the official outcome for the intern, superseding draft assistance according to visibility rules.

FR18: The system can request an AI-generated draft assessment (scores and/or commentary) for a submission subject to policy.

FR19: The system can persist AI draft outputs separately from published mentor outcomes.

FR20: The system can record metadata for each AI invocation sufficient for audit (e.g. which model/instance and when).

FR21: A mentor can complete a review without using AI assistance.

FR22: An intern can view the published scores and mentor feedback for their submission.

FR23: The product can distinguish AI draft material from mentor-published material in the intern experience so drafts are not mistaken for final grades.

FR24: An intern can view the status of their submission through the review lifecycle (e.g. awaiting review, published).

FR25: The product can withhold other interns’ outcomes from a given intern per privacy policy.

FR26: A coordinator (or other auditor role) can access a read-only record of submission, retrieval, AI draft, and published mentor decision for a case.

FR27: The system can show who published each official review and when.

FR28: The system can retain historical versions of outcomes sufficient to explain changes across resubmissions (within retention policy).

FR29: The system can detect inference unavailability or timeouts and signal a degraded mode.

FR30: In degraded mode, a mentor can still publish a mentor-only official review.

FR31: An administrator (or deployment-time configuration, per org) can configure version-control access parameters without embedding secrets in user-visible fields.

FR32: The product can run in a split deployment topology with application, database, and inference runtimes independently replaceable (capability: support that operational model for pilot).

### NonFunctional Requirements

NFR1: Target WCAG 2.1 AA on critical flows where feasible in MVP; at minimum keyboard navigation, labels, and sufficient contrast for login, queue, review, and intern feedback (PRD Domain + Web app requirements).

NFR2: Typical authenticated server page TTFB target under 500 ms P95 on pilot hardware (excluding cold starts).

NFR3: Git source retrieval for a pinned ref succeeds within target SLA (under 60 s P95 for typical MVP repo/path scope) or returns actionable errors.

NFR4: LLM client uses bounded timeouts/retries (Spring AI); UX avoids hung requests and shows progress/degradation where appropriate.

NFR5: Passwords stored hashed; sessions per Spring Security; secure and HttpOnly cookies in non-dev environments.

NFR6: Production error pages do not expose stack traces to end users.

NFR7: Pilot risk posture: zero incidents of accidental export of secrets (API keys, env files) to the LLM.

NFR8: Docker Compose services (app, Postgres, LLM) expose health endpoints suitable for pilot operations; degraded behavior when LLM is down is documented and observable in UI.

NFR9: MVP browser support: latest two stable releases of Chrome, Firefox, Safari, and Edge; mobile browsers best-effort responsive layout (mentor review may remain desktop-first for pilot).

NFR10: Static assets served with appropriate cache headers in production profiles.

NFR11: MVP does not require public SEO or indexable catalog; login-gated application as appropriate.

NFR12: Structured logging in production (e.g. JSON); never log raw tokens, passwords, or full Git/LLM payloads—log identifiers, outcome, and duration.

NFR13: CSRF protection on browser mutations per Spring defaults.

### Additional Requirements

- **Epic 1 / Story 1 anchor:** Scaffold the application from **Spring Initializr** (Java 21+, Spring Boot version confirmed against Spring AI compatibility), with dependencies including web, Thymeleaf, data-jpa, PostgreSQL, Liquibase, security, validation, Actuator, and Spring AI (e.g. `spring-ai-ollama` per architecture); commit generated project as first implementation slice.
- **Persistence:** PostgreSQL as system of record; Liquibase under `src/main/resources/db/changelog/` with ordered master changelog; JPA/Hibernate; schema naming **snake_case** plural tables; decide PK strategy (UUID vs long) in first entity story.
- **Security:** Spring Security session login; BCrypt (or delegating password encoder); role-based access (intern, mentor, administrator, coordinator path for FR26); secrets for Git and LLM from Compose/env only—**not** plaintext in DB.
- **Application style:** MPA—Spring MVC, Thymeleaf, form POST + redirect for writes; `@ControllerAdvice` + user-safe Thymeleaf error pages in production.
- **Integrations:** Git access only via `integration.git` package; **one** HTTP client style for all Git calls (**WebClient** or **RestClient**—lock in first Git story). LLM only via `integration.ai` / Spring AI; no ad-hoc HTTP from controllers.
- **Transactions:** Mentor publish, AI draft persistence, and related audit rows go through **transactional application services**—not controller-direct repository writes for those flows.
- **Frontend assets:** Bootstrap and jQuery via WebJars or `static/`; templates under `templates/` with logical subfolders (`tasks/`, `review/`, `fragments/`, etc.).
- **Observability:** Actuator health for Compose; restrict non-health Actuator exposure in prod; optional MDC/correlation id if introduced.
- **Deployment:** `docker-compose.yml` with replaceable **app**, **db**, **llm** services; `.env.example` documents keys only; **FR32** topology support.
- **Implementation sequence (architecture hint):** (1) scaffold + profiles + Actuator health, (2) Liquibase + users/roles + Security, (3) tasks/submissions/reviews/model metadata tables, (4) Git retrieval + failure UX, (5) Spring AI + degraded mode, (6) Thymeleaf flows (intern, mentor, coordinator read-only), (7) Compose + env/runbook.
- **Data minimization:** Avoid storing raw Git blobs in DB if possible; store metadata + normalized text scope per domain stories; AI draft vs published review must remain distinguishable in persistence.

### UX Design Requirements

No standalone UX specification was present. The following actionable UX requirements were derived from the PRD (web, journeys, domain):

UX-DR1: **Authentication screens** meet baseline accessibility: visible labels, keyboard path to submit, focus states, sufficient contrast (WCAG 2.1 AA goal on critical flows).

UX-DR2: **Mentor queue** uses Bootstrap grid; supports clear ordering (e.g. by due date) and readable status for “awaiting / in review” work (FR13).

UX-DR3: **Mentor review workspace** supports layout suitable for ~1280px width (side-by-side code + rubric); stacks appropriately at smaller breakpoints without losing primary actions.

UX-DR4: **AI draft vs mentor official** is visually explicit everywhere the mentor works: drafts clearly labeled as non-final; published state shows mentor attribution and timestamp (FR27, journeys Jordan/Mara).

UX-DR5: **Intern feedback view** separates **AI draft** (if shown) from **mentor-published** scores and narrative so drafts are not mistaken for final grades (FR22, FR23).

UX-DR6: **Submission lifecycle status** is visible and consistent to interns (e.g. awaiting review, published) including sensible behavior on refresh/concurrency (journey edge case, FR24).

UX-DR7: **Degraded inference:** global or page **banner** plus clear copy that mentor can complete **mentor-only** scoring/publish (FR29, FR30); no unexplained blocking spinners.

UX-DR8: **Git retrieval failures** surface **actionable**, secret-safe messages (e.g. scope, not found) per FR11; mentor/admin path to correct coordinates or retry (FR12).

UX-DR9: **Coordinator/auditor read-only path** (FR26): minimal MVP may be SQL/export or thin read-only UI; must expose submission identity, Git ref, model metadata, draft vs published distinction, publisher id/time.

UX-DR10: **Privacy:** intern views must not expose other interns’ outcomes (FR25).

### FR Coverage Map

FR1: Epic 1 — Sign in with organization credentials  
FR2: Epic 1 — Sign out and end session  
FR3: Epic 1 — Enforce single authenticated identity per action  
FR4: Epic 1 — Role-based feature restriction  
FR5: Epic 1 — Administrator creates/disables/updates accounts  
FR6: Epic 1 — Administrator assigns and changes roles  
FR7: Epic 2 — Create and edit tasks  
FR8: Epic 2 — Intern views assigned tasks  
FR9: Epic 2 — Intern attaches submission with VCS coordinates  
FR10: Epic 3 — Retrieve source from VCS provider  
FR11: Epic 3 — Record retrieval diagnostics without exposing secrets  
FR12: Epic 3 — Re-trigger retrieval or correct coordinates  
FR13: Epic 4 — Mentor queue of submissions  
FR14: Epic 4 — Open submission and inspect source in task context  
FR15: Epic 4 — Structured rubric scores (quality, readability, correctness)  
FR16: Epic 4 — Mentor free-text feedback independent of model text  
FR17: Epic 4 — Publish official review outcome  
FR18: Epic 5 — Request AI draft assessment  
FR19: Epic 5 — Persist AI draft separately from published outcome  
FR20: Epic 5 — Record AI invocation audit metadata  
FR21: Epic 4 / Epic 5 — Complete review without using AI (non-blocking publish; optional AI)  
FR22: Epic 6 — Intern views published scores and mentor feedback  
FR23: Epic 6 — Intern UI distinguishes draft vs published  
FR24: Epic 6 — Intern sees submission lifecycle status  
FR25: Epic 6 — Intern cannot see other interns’ outcomes  
FR26: Epic 7 — Coordinator read-only case record  
FR27: Epic 4 — Show publisher and time for official review  
FR28: Epic 4 — Retain outcome history across resubmissions  
FR29: Epic 5 — Detect inference unavailability/timeouts; signal degraded mode  
FR30: Epic 5 — Mentor-only publish in degraded mode  
FR31: Epic 3 — Configure VCS parameters without secrets in user-visible fields  
FR32: Epic 7 — Split deployment topology (app / db / inference)

## Epic List

### Epic 1: Secure access and administration
**Tracking:** **Done** — `sprint-status.yaml`; stories **1.1–1.5**.

People can sign in with organization accounts, administrators can manage users and roles, and the project has a verified Spring Boot baseline with health checks ready for the rest of the product.

**FRs covered:** FR1, FR2, FR3, FR4, FR5, FR6

### Epic 2: Program tasks and intern submissions
**Tracking:** **Done** — `sprint-status.yaml`; stories **2.1–2.4**.

Mentors or administrators can define internship tasks, assign them to interns, and interns can see their work and attach version-control coordinates to a submission.

**FRs covered:** FR7, FR8, FR9

### Epic 3: Trusted source retrieval from Git
**Tracking:** **Done** — `sprint-status.yaml`; stories **3.1–3.3**.

The system pulls the right source for a submission using organization-configured access, records success or safe diagnostics, and lets authorized users recover from failures.

**FRs covered:** FR10, FR11, FR12, FR31

### Epic 4: Mentor review workspace and official outcomes
**Tracking:** **Done** — stories 4.1–4.5 implemented; artifacts marked `done`. Details in `_bmad-output/implementation-artifacts/4-*.md`.

Mentors can work through a queue, read code in context, score and comment, publish the official outcome with clear ownership and history—even before any AI assistance is enabled.

**FRs covered:** FR13, FR14, FR15, FR16, FR17, FR21 (publish path without AI), FR27, FR28

### Epic 5: AI-assisted drafts and resilient inference
**Tracking:** In progress — see `sprint-status.yaml` (stories 5.1–5.4 ready-for-dev).

The product requests and stores model drafts separately from mentor verdicts, captures audit metadata, and degrades gracefully when inference is unavailable—without blocking mentor publish.

**FRs covered:** FR18, FR19, FR20, FR21 (optional draft path), FR29, FR30

### Epic 6: Intern transparency and privacy-safe feedback
**Tracking:** In progress — see `sprint-status.yaml` (stories 6.1–6.4 ready-for-dev).

Interns see lifecycle status, published mentor judgment, and clearly labeled drafts; they never see other interns’ outcomes.

**FRs covered:** FR22, FR23, FR24, FR25

### Epic 7: Audit visibility and pilot-ready deployment
**Tracking:** In progress — see `sprint-status.yaml` (stories 7.1–7.3 ready-for-dev).

Coordinators can inspect a case record for accountability, and operators can run the app, database, and model as separate composable services.

**FRs covered:** FR26, FR32

---

## Epic 1: Secure access and administration

**Tracking:** **Done** — `sprint-status.yaml`; stories **1.1–1.5**.

People can sign in with organization accounts, administrators can manage users and roles, and the project has a verified Spring Boot baseline with health checks ready for the rest of the product.

### Story 1.1: Bootstrap application from Spring Initializr

As a **developer**,
I want **a generated Spring Boot 3.x / Java 21 project with Thymeleaf, JPA, PostgreSQL, Liquibase, Security, Validation, Actuator, and Spring AI dependencies**,
So that **the codebase matches the architecture baseline and later stories can add domain features incrementally**.

**Implements:** Architecture (Initializr), NFR2 (baseline for TTFB work), NFR10 (static resource pipeline placeholder).

**Acceptance Criteria:**

**Given** the team has chosen a Boot version compatible with Spring AI  
**When** the project is generated (curl/Initializr) and imported as Maven  
**Then** the application starts with `dev` profile and exposes an Actuator **health** endpoint suitable for Compose checks  
**And** package layout matches architecture guidance (`com.examinai.app`, `config`, `domain`, `web`, `integration` packages may start empty)

**Given** production profile configuration exists  
**When** the app runs with `prod`-oriented settings  
**Then** developer-only tools are not required for a minimal health check smoke test  

---

### Story 1.2: Database connectivity and first Liquibase changelog

As a **developer**,
I want **PostgreSQL connectivity and an empty or minimal Liquibase master changelog**,
So that **later stories can add only the tables they need**.

**Implements:** Additional requirements (Liquibase layout), Architecture (changelog numbering).

**Acceptance Criteria:**

**Given** JDBC URL and credentials from configuration (env or local dev)  
**When** the application starts  
**Then** Liquibase runs successfully against the database and the changelog location matches `src/main/resources/db/changelog/` conventions  
**And** no domain tables beyond what this story needs are created (optional: single `databasechangelog` only)

---

### Story 1.3: User identity persistence and password hashing

As a **developer**,
I want **user records stored with hashed passwords and role linkage**,
So that **Spring Security can authenticate against the database**.

**Implements:** FR1 (foundation), FR5–FR6 (data model), NFR5, Architecture (BCrypt/delegating encoder).

**Acceptance Criteria:**

**Given** Liquibase changes for `users` and role assignment (or join table) per naming conventions  
**When** migrations apply  
**Then** passwords are never stored plaintext and schema uses snake_case plural table names  
**And** at least one seed or migration path can create an initial admin for local pilot (documented only; no secrets in repo)

---

### Story 1.4: Session login, logout, and CSRF-safe navigation

As a **user**,
I want **to sign in and sign out with a server-side session**,
So that **my actions run under one authenticated identity inside the app**.

**Implements:** FR1, FR2, FR3, NFR5, NFR13, UX-DR1 (login flow).

**Acceptance Criteria:**

**Given** a user exists with a valid password  
**When** they submit the login form  
**Then** they are authenticated and redirected to a role-appropriate home (placeholder landing is acceptable)  
**And** logout clears the session and subsequent protected requests are denied (**FR2**)

**Given** an unauthenticated client  
**When** they open protected URLs  
**Then** they are redirected to login and cannot perform mutating actions (**FR3**)

**Given** the login page  
**When** navigated by keyboard and screen reader–friendly labels  
**Then** primary fields have visible labels and submit is reachable without mouse (**UX-DR1**, **NFR1** baseline)

**Given** a mutating form in the app  
**When** CSRF protection is enabled (Spring default)  
**Then** posts include a valid token and forged posts fail (**NFR13**)

---

### Story 1.5: Role-based access and administrator user management UI

As an **administrator**,
I want **to create, disable, and update accounts and assign roles**,
So that **interns, mentors, and coordinators exist with correct permissions**.

**Implements:** FR4, FR5, FR6.

**Acceptance Criteria:**

**Given** a user with administrator role  
**When** they open the admin user management screens  
**Then** they can create a user, disable or update account state, and assign roles **FR5**, **FR6**  
**And** non-admin users receive **403** (or equivalent) on those routes (**FR4**)

**Given** an intern account  
**When** they attempt admin actions  
**Then** access is denied and no data is leaked in error messages (**FR4**, **NFR6** for admin errors—not stack traces in prod profile)

---

## Epic 2: Program tasks and intern submissions

**Tracking:** **Done** — `sprint-status.yaml`; stories **2.1–2.4**.

Mentors or administrators can define internship tasks, assign them to interns, and interns can see their work and attach version-control coordinates to a submission.

### Story 2.1: Create and edit tasks

As a **mentor or administrator**,
I want **to create and edit tasks with instructions and due expectations**,
So that **interns know what to build and by when**.

**Implements:** FR7.

**Acceptance Criteria:**

**Given** a mentor or admin (per product policy implemented as allowed roles)  
**When** they create or update a task with title, description/instructions, and due date fields  
**Then** the task is persisted and listed on a mentor/admin task index  
**And** validation errors are shown on the form without exposing internal details (**NFR6**)

---

### Story 2.2: Assign tasks to interns

As a **mentor or administrator**,
I want **to assign a task to one or more interns**,
So that **each intern sees only their own assigned work later**.

**Implements:** FR8 (foundation), aligns with program operations.

**Acceptance Criteria:**

**Given** an existing task and intern users  
**When** the authorized user assigns the task to selected interns  
**Then** assignments persist and can be listed for that task  
**And** removing or editing assignments is supported within MVP scope (document behavior)

---

### Story 2.3: Intern task list

As an **intern**,
I want **to see tasks assigned to me**,
So that **I know what I must deliver**.

**Implements:** FR8.

**Acceptance Criteria:**

**Given** an authenticated intern with at least one assignment  
**When** they open their task list  
**Then** they see only their assigned tasks with due information  
**And** they do not see tasks assigned only to other interns (**FR25** precursor)

---

### Story 2.4: Attach submission with version-control coordinates

As an **intern**,
I want **to attach a submission to my task by supplying repository and revision (and scope fields as defined in this story)**,
So that **mentors can review the exact code I point to**.

**Implements:** FR9, **NFR3** (inputs for later fetch SLA).

**Acceptance Criteria:**

**Given** an intern and an assigned task  
**When** they submit coordinates (e.g. repo identifier, commit SHA, optional path scope per MVP rules)  
**Then** a submission record is created in **draft or submitted** state suitable for downstream retrieval  
**And** they can correct coordinates on a new submission revision if policy allows (prepare for **FR28** without implementing full history here if out of scope—at minimum newest submission wins for MVP unless story expanded)

---

## Epic 3: Trusted source retrieval from Git

**Tracking:** **Done** — `sprint-status.yaml`; stories **3.1–3.3**.

The system pulls the right source for a submission using organization-configured access, records success or safe diagnostics, and lets authorized users recover from failures.

### Story 3.1: Version-control configuration and secret hygiene

As an **operator or administrator**,
I want **Git host parameters and tokens supplied via deployment configuration—not user-visible fields**,
So that **tokens never appear in the UI or logs**.

**Implements:** FR31, Additional requirements (secrets in Compose/env), **NFR12**.

**Acceptance Criteria:**

**Given** environment variables or Compose secrets for provider base URL and token  
**When** the application starts  
**Then** integration code reads credentials only from configuration beans—never from request parameters or user profile fields (**FR31**)

**Given** a failed Git call  
**When** the system logs the event  
**Then** logs contain no token values or full secret-bearing URLs (**NFR12**)

---

### Story 3.2: Git integration client and source fetch

As the **system**,
I want **to retrieve normalized source text (or scoped files) for a submission using one HTTP client style**,
So that **mentors can review real code in Epic 4**.

**Implements:** FR10, **NFR3**, Architecture (single WebClient **or** RestClient—pick one and document).

**Acceptance Criteria:**

**Given** a submission with valid coordinates and working credentials  
**When** retrieval runs  
**Then** retrieved content (or metadata + text per implementation decision) is stored per architecture data-minimization guidance  
**And** typical pilot-sized scope completes within the PRD Git SLA target or surfaces timeout as a controlled failure (**NFR3**)

**Given** provider rate or transient errors  
**When** retrieval runs with configured retry policy  
**Then** behavior is bounded and does not hang the request thread indefinitely

---

### Story 3.3: Retrieval status, safe diagnostics, and retry UX

As a **mentor or authorized user**,
I want **clear status for fetch success or failure and a way to retry or fix coordinates**,
So that **I can recover without guessing**.

**Implements:** FR11, FR12, UX-DR8.

**Acceptance Criteria:**

**Given** a failed retrieval (e.g. 403, 404, scope error)  
**When** a mentor views the submission  
**Then** they see an actionable, secret-safe message (e.g. “commit not found”, “credential lacks scope”) (**FR11**, **UX-DR8**)

**Given** a failed or stale retrieval  
**When** an authorized user triggers re-fetch or updates coordinates per policy  
**Then** the system attempts retrieval again and updates status (**FR12**)  
**And** prior successful snapshots are not corrupted by a failed retry (align with journey recovery expectations)

---

## Epic 4: Mentor review workspace and official outcomes

**Tracking:** **Done** — `sprint-status.yaml`; implementation artifacts `4-1` … `4-5`.

Mentors can work through a queue, read code in context, score and comment, publish the official outcome with clear ownership and history—even before any AI assistance is enabled.

### Story 4.1: Mentor submission queue

As a **mentor**,
I want **a list of submissions that need attention**,
So that **I can work my review queue efficiently**.

**Implements:** FR13, UX-DR2, **NFR1** (queue keyboard/contrast baseline).

**Acceptance Criteria:**

**Given** submissions exist in non-final review states  
**When** a mentor opens the queue  
**Then** rows show task, intern, due context, and status (“awaiting review”, “in progress”) (**FR13**)

**Given** the queue page  
**When** viewed on desktop widths  
**Then** Bootstrap grid layout supports scanning and ordering by due date where available (**UX-DR2**)

---

### Story 4.2: Submission review detail with source in context

As a **mentor**,
I want **to open a submission and read retrieved source beside the task brief**,
So that **I grade the right artifact in context**.

**Implements:** FR14, UX-DR3.

**Acceptance Criteria:**

**Given** a submission with successful retrieval  
**When** the mentor opens the review detail view  
**Then** task instructions and source content (or file list with viewer) are both visible in a layout comfortable at ~1280px and stacks on smaller widths (**FR14**, **UX-DR3**)

**Given** retrieval failed  
**When** the mentor opens the detail view  
**Then** they see failure state from Epic 3 rather than empty success UI (**FR11** continuity)

---

### Story 4.3: Rubric scores and mentor narrative (pre-publish)

As a **mentor**,
I want **to enter structured scores and my own free-text feedback**,
So that **my judgment is captured before I publish**.

**Implements:** FR15, FR16.

**Acceptance Criteria:**

**Given** the review detail view  
**When** the mentor adjusts quality, readability, and correctness scores and enters free-text feedback  
**Then** values persist as **unpublished** mentor work-in-progress (**FR15**, **FR16**)

**Given** the mentor clears or invalidates numeric fields  
**When** they attempt to save  
**Then** validation errors are shown accessibly per **NFR1**

---

### Story 4.4: Publish official review with provenance

As a **mentor**,
I want **to publish my review as the official outcome**,
So that **interns and auditors see my decision—not the model’s**.

**Implements:** FR17, FR27, **FR21** (no AI required to publish), Architecture (transactional publish service).

**Acceptance Criteria:**

**Given** valid rubric and narrative meeting validation rules  
**When** the mentor chooses Publish  
**Then** a transactional service records the published outcome, publishing mentor identity, and timestamp (**FR17**, **FR27**)  
**And** publish succeeds even if no AI draft exists (**FR21**)

**Given** concurrent intern refresh scenarios  
**When** publish completes  
**Then** status transitions are consistent (no “final grade” flash before publish)

---

### Story 4.5: Outcome and submission history across resubmissions

As a **mentor or auditor (read-only in Epic 7)**,
I want **history of outcomes tied to submission revisions**,
So that **resubmissions are explainable over time**.

**Implements:** FR28.

**Acceptance Criteria:**

**Given** an intern submits a new revision after feedback  
**When** users with rights view the case timeline  
**Then** prior published outcomes remain associated with the correct submission version (**FR28**)  
**And** the UI makes clear which outcome applies to which revision (mentor-facing minimum; intern view refined in Epic 6)

---

## Epic 5: AI-assisted drafts and resilient inference

**Tracking:** In progress — `sprint-status.yaml` (stories 5.1–5.4 ready-for-dev).

The product requests and stores model drafts separately from mentor verdicts, captures audit metadata, and degrades gracefully when inference is unavailable—without blocking mentor publish.

### Story 5.1: Request AI draft assessment via Spring AI

As a **mentor**,
I want **to request an AI draft assessment for the current submission**,
So that **I get assistive signals without ceding final judgment**.

**Implements:** FR18, **NFR4**, **NFR7** (never send env/secrets file contents; minimal payload policy documented).

**Acceptance Criteria:**

**Given** a submission with retrievable source within policy  
**When** the mentor triggers “Generate AI draft”  
**Then** the system calls Spring AI with bounded timeout/retry settings (**NFR4**)  
**And** the request payload excludes secrets and follows data-minimization rules documented for pilot (**NFR7**)

---

### Story 5.2: Persist draft separately and record invocation metadata

As the **system**,
I want **to store AI output and model metadata apart from published mentor rows**,
So that **audits can answer what the model said and when**.

**Implements:** FR19, FR20, UX-DR4 (mentor UI labels non-final draft).

**Acceptance Criteria:**

**Given** a successful model response  
**When** results are saved  
**Then** draft scores/text live in distinct persistence from published review data (**FR19**)  
**And** each invocation records model identity/version identifier and timestamp, with optional prompt hash if implemented (**FR20**)

**Given** the mentor review screen  
**When** a draft exists  
**Then** it is clearly labeled as **not final** relative to published fields (**UX-DR4**)

---

### Story 5.3: Optional AI—mentor workflow without model dependence

As a **mentor**,
I want **to ignore or skip AI drafts entirely**,
So that **I am never forced to rely on the model**.

**Implements:** FR21.

**Acceptance Criteria:**

**Given** no draft has been generated  
**When** the mentor completes scoring and publishes  
**Then** publish behavior matches Epic 4 and no AI fields are required (**FR21**)

**Given** a draft exists  
**When** the mentor overrides all visible draft-derived suggestions before publish  
**Then** published outcome reflects mentor values; draft remains visible only where policy allows (intern rules in Epic 6)

---

### Story 5.4: Degraded inference detection, banner, and mentor-only publish

As a **mentor**,
I want **clear signals when the model is unavailable and a path to publish anyway**,
So that **reviews do not stall on infrastructure issues**.

**Implements:** FR29, FR30, UX-DR7, **NFR8** (observable degraded path).

**Acceptance Criteria:**

**Given** the LLM endpoint is down or requests time out  
**When** the mentor uses the review workspace  
**Then** a **degraded inference** banner (or equivalent global flag) explains the situation (**FR29**, **UX-DR7**)  
**And** mentor-only publish from Epic 4 remains available without AI (**FR30**)

**Given** degraded mode  
**When** an intern later views the case (policy permitting drafts)  
**Then** degraded labeling does not imply a final model grade (**FR23** alignment—defer exact intern copy to Epic 6 if needed)

---

## Epic 6: Intern transparency and privacy-safe feedback

**Tracking:** In progress — `sprint-status.yaml` (stories 6.1–6.4 ready-for-dev).

Interns see lifecycle status, published mentor judgment, and clearly labeled drafts; they never see other interns’ outcomes.

### Story 6.1: Intern feedback view for published outcomes

As an **intern**,
I want **to see published scores and mentor feedback for my submission**,
So that **I know what the mentor decided**.

**Implements:** FR22, UX-DR5.

**Acceptance Criteria:**

**Given** a published review for my submission revision  
**When** I open my submission feedback page  
**Then** I see mentor-published rubric scores and narrative prominently (**FR22**, **UX-DR5**)

**Given** no publish yet  
**When** I open the page  
**Then** I do not see unpublished mentor work-in-progress as if it were final

---

### Story 6.2: Draft vs published labeling for interns

As an **intern**,
I want **AI-assisted material clearly marked as draft when policy shows it at all**,
So that **I do not confuse model text with my official grade**.

**Implements:** FR23, UX-DR5, UX-DR6.

**Acceptance Criteria:**

**Given** policy allows intern visibility of AI draft content before publish (if ever) or after publish as supplementary context  
**When** I view the page  
**Then** draft sections are visually and textually distinct from “Official mentor feedback” (**FR23**, **UX-DR5**)

**Given** only published mentor outcome exists  
**When** I view the page  
**Then** I do not see hallucinated “AI final grade” patterns—mentor block is primary (**FR23**)

---

### Story 6.3: Submission lifecycle status

As an **intern**,
I want **to see where my submission sits in the review lifecycle**,
So that **I know whether I should wait or revise**.

**Implements:** FR24, UX-DR6.

**Acceptance Criteria:**

**Given** states such as submitted, retrieval failed, awaiting review, in review, published (MVP-defined enum)  
**When** I view my submission  
**Then** the current status is shown consistently across refreshes (**FR24**, **UX-DR6**)

---

### Story 6.4: Privacy isolation between interns

As an **intern**,
I want **guarantees I only see my own outcomes**,
So that **peer grades stay private**.

**Implements:** FR25, UX-DR10.

**Acceptance Criteria:**

**Given** another intern’s submission ID or deep link  
**When** I attempt access  
**Then** I receive **403** or “not found” per security policy and see no scores (**FR25**, **UX-DR10**)

**Given** shared task lists by policy  
**When** I browse  
**Then** I never see other interns’ rubric outcomes or mentor feedback

---

## Epic 7: Audit visibility and pilot-ready deployment

**Tracking:** In progress — `sprint-status.yaml` (stories 7.1–7.3 ready-for-dev).

Coordinators can inspect a case record for accountability, and operators can run the app, database, and model as separate composable services.

### Story 7.1: Coordinator read-only case record

As a **coordinator**,
I want **a read-only view of submission, retrieval, AI draft, and published mentor decision for one case**,
So that **I can answer audit questions without write access**.

**Implements:** FR26, UX-DR9.

**Acceptance Criteria:**

**Given** a user with coordinator (or auditor) role  
**When** they open a case by submission identifier  
**Then** they see Git ref/coordinates, retrieval outcome summary, AI invocation metadata, draft vs published distinction, publisher id and time (**FR26**, **UX-DR9**)  
**And** coordinators cannot mutate reviews or scores through this surface (read-only)

**Given** a user without coordinator role  
**When** they attempt the same URL  
**Then** access is denied (**FR4** extension)

---

### Story 7.2: Docker Compose for application, database, and LLM

As an **operator**,
I want **separate containers for app, Postgres, and LLM**,
So that **we can replace or scale pieces independently in pilot**.

**Implements:** FR32, **NFR8**, Architecture (topology).

**Acceptance Criteria:**

**Given** `docker-compose.yml` describing **app**, **db**, and **llm** services  
**When** operators run `docker compose up` with documented env  
**Then** all three start and the app reaches JDBC + AI endpoint configuration successfully (**FR32**)

**Given** the LLM container is stopped  
**When** the app runs health checks and mentor flows  
**Then** degraded behavior matches Epic 5 expectations (banner + mentor publish) (**NFR8**)

---

### Story 7.3: Pilot operations runbook and environment documentation

As an **operator**,
I want **`.env.example` and a short runbook for smoke tests (login, fetch, optional LLM call)**,
So that **new laptops can join the pilot safely**.

**Implements:** Additional requirements (runbook), **NFR8**, **NFR12**, Architecture (no secrets in repo).

**Acceptance Criteria:**

**Given** `.env.example` listing non-secret keys and descriptions  
**When** a developer configures secrets locally  
**Then** no credentials are required to be committed to git  
**And** runbook documents health endpoints, typical Git failure codes, and degraded LLM behavior

**Given** production-oriented profile  
**When** errors occur  
**Then** user-facing pages avoid stack traces (**NFR6**) and Actuator exposure is restricted as per architecture

---

## Final validation summary

**FR coverage:** FR1–FR32 each map to at least one epic in the **FR Coverage Map** and are exercised by one or more acceptance criteria. **FR21** is satisfied by Epic 4 publish without AI and Epic 5 optional draft behavior.

**Starter template:** Epic 1 Story 1 matches the architecture mandate (Spring Initializr scaffold first).

**Incremental schema:** Tables and integrations are introduced in the first epic/story that needs them; there is no “create all tables in Story 1.1” pattern.

**Story ordering:** Within each epic, later stories build only on earlier ones; epics chain Foundation → Tasks → Git → Review → AI → Intern → Audit/Deploy without backward dependencies.

**UX-DR coverage:** UX-DR1–UX-DR10 are distributed across Stories 1.4, 3.3, 4.1–4.4, 5.2–5.4, 6.x, 7.1 (see story text).

**NFR coverage:** Security, CSRF, cookies, logging hygiene, Git/LLM timeouts, WCAG-oriented ACs, Compose health, and production error handling are reflected across Epics 1, 3, 5, 6, and 7.

---

**Workflow note:** Planning epics and stories are complete. **Delivery status** is maintained in `_bmad-output/implementation-artifacts/sprint-status.yaml` and summarized in **Implementation status** above. For BMad **help** with next steps, use the `bmad-help` skill.

**Select an Option:** [A] Advanced Elicitation · [P] Party Mode · [C] **Complete Workflow** (acknowledge final validation; no further automated steps in this document)
