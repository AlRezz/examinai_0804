---
stepsCompleted:
  - step-01-init
  - step-02-discovery
  - step-02b-vision
  - step-02c-executive-summary
  - step-03-success
  - step-04-journeys
  - step-05-domain
  - step-06-innovation
  - step-07-project-type
  - step-08-scoping
  - step-09-functional
  - step-10-nonfunctional
  - step-11-polish
  - step-12-complete
innovationSection: skipped
nfrSection: omitted-by-stakeholder
prdWorkflowCompletedAt: '2026-04-08'
inputDocuments:
  - _bmad-output/brainstorming/brainstorming-session-2026-04-08-070204.md
workflowType: prd
documentCounts:
  briefCount: 0
  researchCount: 0
  brainstormingCount: 1
  projectDocsCount: 0
classification:
  projectType: web_app
  domain: edtech
  complexity: medium
  projectContext: greenfield
---

# Product Requirements Document - examinai_0804

**Author:** Alex
**Date:** April 8, 2026

## PRD scope notes

- A dedicated **Non-functional requirements** section was **omitted** by stakeholder choice. **Quality targets** still appear under **Success criteria** (technical + pilot metrics) and **Web application specific requirements** (performance, accessibility).

## Executive Summary

**Examinai** (working name: **examinai_0804**) is a **greenfield web application** that helps **mentors** review **interns’** submitted **source code** and produce **structured assessments** of **quality**, **readability**, and **correctness**. The system **assists** mentors with a **local large language model** (target: **deepseek-r1:8b** in a **dedicated container**) integrated via **Spring AI**, while keeping **mentors authoritative**: final scores, overrides, and comments are first-class; model output is **draft assistance**, not the official verdict. Code is **ingested from Git via API**; the stack is **Spring** (Java **21+**), **PostgreSQL** with **Liquibase**, **Hibernate** for persistence of **mentors**, **interns**, **tasks**, **sources**, and related entities, **Spring Security** with **database-backed username/password** authentication, and a **simple server-rendered UI** (**Thymeleaf**, **Bootstrap**, **jQuery**) deployed with **Docker Compose** as **separate images** for **application**, **database**, and **LLM**. Target context is **edtech / internship programs** (medium domain complexity): organizations that need **consistent rubrics**, **faster mentor throughput**, and **privacy-preserving** (on-prem style) AI—not generic public-cloud code grading.

**Primary users:** **Mentors** (review, calibrate, finalize) and **interns** (submit work linked to tasks/repos, consume feedback). **Organizations** gain **audit-friendly** artifacts (what was scored, by whom, with which model revision) if implemented in later requirements.

**Problem:** Manual review **does not scale** with cohort size; ad-hoc feedback is **inconsistent**; sending student code to **external LLMs** is often **unacceptable**. Surface need: “score code with AI.” Deeper need: **scale good mentoring**—clear criteria, less busywork, **trust** and **clarity** for both sides.

### What Makes This Special

1. **Mentor-sovereign workflow** — Clear separation of **AI draft** vs **published** mentor judgment; emphasis on **override**, **notes**, and **disagreement** handling (per brainstorm themes).
2. **Privacy-oriented deployment** — **Local model** in its own image, **minimal** code/data crossing the LLM boundary, **no** SSO scope in v1 reduces moving parts while **credentials stay in DB** under **Spring Security**.
3. **Program-shaped product** — **Tasks**, **Git-backed** submissions, and **mentor queues** align with **real internship** operations rather than a disconnected “paste code” toy.

**Core insight:** Value is realized only when **interns** never confuse model text with **final judgment**, and **mentors** trust the system enough to adopt it—**UX labeling**, persistence of **human decisions**, and **model/version provenance** matter as much as raw model quality.

## Project Classification

| Dimension | Value |
|-----------|--------|
| **Project type** | **Web application** (`web_app`) — server-rendered UI, browser clients |
| **Domain** | **Edtech** — mentor–intern learning and assessment |
| **Complexity** | **Medium** — privacy/accessibility/cultural expectations for educational settings; not Phase-1 regulated verticals unless scope expands |
| **Project context** | **Greenfield** — no existing product docs in repo; brainstorming artifact is primary input |

## Success Criteria

### User success

- **Mentors** can open a **task submission**, see **AI-assisted** signals and text for **quality / readability / correctness**, and record a **final mentor judgment** (scores + comments) with a clear **“official”** vs **“AI draft”** distinction in under **one focused session** (target: **≤ 20 minutes** for a typical small submission once familiar with the UI).
- **Mentors** report **confidence** that the **published** outcome reflects **their** decision, not the model’s—operationally: **100%** of published scores are attributable to a **mentor action** (MVP assumes **mentor sign-off**).
- **Interns** can see **explained** feedback (criteria-linked) and **do not** treat model output as final unless **labeled** and **policy allows**.
- **Interns** experience **predictable** behavior when the **LLM service is unavailable**: they still see **task state** and **mentor path** (e.g. **mentor-only** review) without **data loss** of submitted artifacts.

### Business success

- **Program / org** reduces **mentor time per review** vs purely manual baseline (pilot target: **≥ 25%** reduction for comparable submissions over **4–8 weeks** with **≥ 3** mentors and **≥ 10** interns).
- **Adoption**: **≥ 80%** of pilot mentors **complete** at least **one** full review cycle in-tool during pilot.
- **Risk posture**: **zero** pilot incidents of **accidental** export of **secrets** (API keys, env files) to the LLM.

### Technical success

- **Compose**: **app**, **Postgres**, and **LLM** containers **start** and **communicate**; app **degrades gracefully** when LLM is **down** or **timeouts** (Spring AI behavior **documented**).
- **Persistence**: **Submissions**, **scores**, **mentor decisions**, and **model invocation metadata** (model **version** / endpoint id, **timestamp**, optional **prompt hash**) are **stored** for audit.
- **Git integration**: **Authorized** fetch for a **pinned** ref succeeds within **SLA** (e.g. **&lt; 60s** P95 for typical MVP repo size) or returns **actionable** errors.
- **Security baseline**: **Passwords** **hashed**; **sessions** per Spring Security; **no plaintext** Git tokens in DB—secrets via **Compose** secrets/env.

### Measurable outcomes (pilot-level)

| Outcome | Signal |
|--------|--------|
| Throughput | Reviews **completed / week / mentor** vs baseline |
| Consistency | **Variance** across mentors for similar tasks (directionally lower after calibration, if added) |
| Trust | Mentor **override** rate on AI suggestions; qualitative debrief |
| Reliability | App vs LLM **uptime**; **%** reviews on **degraded** path |
| Audit | **100%** published reviews have **mentor id**, **timestamps**, **submission version**, **model metadata** |

## Product scope

### MVP — minimum viable product

- **Roles:** mentor + intern + optional minimal **admin** (seed/DB).
- **Auth:** **DB-backed** login (**Spring Security**).
- **Tasks** linked to **Git**; **fetch** via **API**; show scope per MVP definition.
- **AI:** **Spring AI** → **local LLM**; **structured** + narrative output for **three dimensions**; persist draft + **mentor final**.
- **UI:** **Thymeleaf**, **Bootstrap**, **jQuery**; mentor **review** + intern **feedback** view.
- **Data:** **PostgreSQL**, **Liquibase**, **Hibernate**; **users**, **tasks**, **submissions/sources**, **reviews/scores**, **AI metadata**.
- **Deploy:** **Docker Compose**; **three** images (app / db / llm).

### Growth (post-MVP)

- **Rubric calibration** per cohort/language; **privacy** modes for peer comparison.
- **Richer Git** providers, **webhooks**, **IDE/PR** flows; **SSO**, **tenants**, **admin** console, **retention** policy UI.

### Vision (future)

- **Multi-language**, **custom rubrics**, **originality** as a **separate**, ethically bounded module.
- **Program analytics** (skills, mentor load).

## User journeys

### Journey 1 — Jordan (mentor): “Friday afternoon review queue”

**Opening:** Jordan has **six** intern submissions stuck in email and sticky notes. They’re worried about being **unfair** or **inconsistent** and don’t want to paste student code into a **public** chatbot.

**Rising action:** Jordan logs into **Examinai**, sees a **queue** ordered by due date. They open **Mara’s** task; the app has already **pulled** the right **commit** from Git. A panel shows **AI draft** scores and comments for **quality, readability, correctness**, clearly **labeled** as not final. Jordan scan-reads the code, tweaks two rubric scores, adds a short **mentor note**, and clicks **Publish review**.

**Climax:** The UI makes it obvious **what changed** versus the AI draft; the **published** row shows **Jordan** as decision owner with a **timestamp**.

**Resolution:** Jordan clears the queue in **under two hours**, trusts the export for the program lead, and stops feeling like the “bad guy” because feedback is tied to **visible criteria**.

**Failure / recovery:** If the **LLM container** is down, Jordan sees a **banner** and completes a **mentor-only** review with a flag **“AI unavailable—human scoring only.”** No blocking spinner without explanation.

### Journey 2 — Mara (intern): “Is this my grade or the robot’s?”

**Opening:** Mara just pushed a fix and is **anxious**—last term a tool **humiliated** her with wrong “correctness” in front of peers.

**Rising action:** She logs in, opens **her task**. She sees **submission received**, optional **AI draft** section behind a **“draft / not final”** pattern, and a state: **“Awaiting mentor review.”** After Jordan publishes, Mara sees **final** scores and comments, with **mentor** attribution and **criteria** references.

**Climax:** She understands **exactly** what to change next—not because the model said so, but because **mentor + rubric** say so.

**Resolution:** Mara revises and resubmits; history shows **versions** without exposing other interns’ scores.

**Edge case:** If Mara refreshes while the mentor is editing, she sees **consistent** status (no flicker that implies a final grade that isn’t published).

### Journey 3 — Sam (program coordinator): “Prove it for the lead”

**Opening:** Sam doesn’t review code daily but must **account** for how grades were produced and that **student work** stayed **inside** agreed boundaries.

**Rising action:** Sam uses a **lightweight** path (MVP: **read-only SQL/export** or minimal **admin** screen—per later FRs) to open a **review record**: submission id, **Git ref**, **model version**, **prompt hash** (if captured), **AI draft vs published** diff, **mentor id**.

**Climax:** Sam answers the director’s “did AI grade this?” with **no**—**mentor published**; AI was **assistive** only, with **artifact trail**.

**Resolution:** Pilot continues with **audit comfort**; policy for **retention** can be tightened post-MVP.

### Journey 4 — DevOps / builder: “Compose up, keys safe”

**Opening:** The team brings up a new laptop for a pilot. They must not check **Git PATs** into git.

**Rising action:** They copy **`.env` / Compose secrets**, run **docker compose up**, verify **health** endpoints: **app**, **db**, **llm**. They run a **smoke test**: login, **fetch** sample repo, **one** LLM call with **timeout**.

**Climax:** **Network** isolation: LLM has **no** egress to surprise hosts; app has **documented** env vars.

**Resolution:** Pilot unblocked; **on-call** knows **degraded mode** behavior when **LLM** OOMs or restarts.

### Journey 5 — Git API integration (system-adjacent)

**Opening:** A submission supplies **org/repo** + **commit ref** + **path scope** (required file path for retrieval).

**Rising action:** App uses **stored credential** (secret) to call the **provider REST API** (commits + optional contents/raw URLs per implementation), validates **rate/scope**, stores **retrieval state** and **normalized text** for review (per domain design).

**Climax:** On **403/404**, mentor sees **actionable** error (“token lacks repo scope” / “commit not found”).

**Recovery:** Mentor or admin **fixes** token or **ref** without corrupting prior submissions.

### Journey requirements summary

| Capability area | Revealed by |
|-----------------|-------------|
| **Mentor queue + review workspace** | Jordan |
| **AI draft vs publish + attribution** | Jordan, Mara |
| **Intern feedback + history/versioning** | Mara |
| **Degraded mode (LLM down)** | Jordan, DevOps |
| **Audit / review record (export or admin)** | Sam |
| **Git fetch, errors, secrets handling** | Integration journey, DevOps |
| **Compose deploy + health + observability** | DevOps |

## Domain-specific requirements

### Scope note

Formal **compliance/regulatory** program work (statutory deep dives, COPPA/FERPA analysis, etc.) is **not required in this PRD** per product owner direction. The **deploying organization** owns legal interpretation and policy. The product still implements **baseline security, privacy, and safe UX** described elsewhere (auth, secrets, data minimization to the LLM, mentor publish gate).

### Technical constraints

- **Accessibility:** Target **WCAG 2.1 AA** on critical flows where feasible in MVP; at minimum **keyboard**, **labels**, and **contrast** for **login**, **queue**, **review**, and **intern feedback**.
- **Safe feedback:** Treat model output as **draft**; **mentor-published** text is what interns should rely on for “official” narrative; reduce exposure to **harmful** or **inappropriately harsh** raw model tone.

### Integration requirements

- **No CSV import:** roster and bulk file ingestion are **out of scope**; users/tasks via **in-app** creation or **seed** data.
- **Git:** Credential handling, retrieval errors, and audit alignment—see **User journeys**, **Success criteria**, and **FR10–FR12**, **FR31**.

### Risk mitigations

| Risk | Mitigation |
|------|------------|
| **Model tone / bias** | **Mentor publish** gate; **draft** labeling; mentor can edit intern-visible feedback |
| **Data handling** | **Local LLM**; **minimal** payload to model; secrets via **Compose**/env—not in DB plaintext |

## Web application specific requirements

### Project-type overview

Examinai is an **MPA** delivered as **HTML over HTTP**: **Spring MVC + Thymeleaf**, **Bootstrap** layout, and **jQuery** for progressive enhancement. Primary users are **authenticated mentors and interns**; there is no MVP requirement for a separate SPA or public SEO landing beyond optional login branding.

### Technical architecture considerations

- **Rendering:** **Server-side** templates; form posts and redirects for mutations; **CSRF** protection per Spring defaults.
- **Assets:** **Bootstrap** + **jQuery** via **WebJars** or static resources under configured resource handlers; **cache** headers for static assets in production profiles.
- **Sessions:** **Server session** (cookie) for **Spring Security**; **secure** and **HttpOnly** cookies in non-dev environments.

### Browser matrix

| Browser | MVP support |
|---------|-------------|
| Chrome | Latest 2 stable |
| Firefox | Latest 2 stable |
| Safari | Latest 2 stable |
| Edge | Latest 2 stable |

Mobile browsers: **best-effort** responsive layout; full mentor **review** UX may remain **desktop-first** for pilot if needed.

### Responsive design

- **Bootstrap** grid for **queue**, **review**, and **feedback** views.
- **Minimum width** target ~**1280px** for comfortable **side-by-side** code + rubric; **stacked** layout below breakpoint for intern views.

### Performance targets

- **Server:** typical authenticated page **TTFB** target **< 500 ms** P95 on pilot hardware (excluding cold starts).
- **Git fetch:** aligned with success criteria (**< 60 s** P95 for typical MVP repo/path scope).
- **LLM:** **bounded** client timeouts/retries in **Spring AI**; **non-blocking** UX where possible (show progress, avoid hung requests).

### SEO strategy

- **MVP:** **no** public indexable catalog; **robots**/login gate as appropriate; **no** SEO-driven information architecture.

### Accessibility level

- **Goal:** **WCAG 2.1 AA** on **login**, **task list**, **review**, **publish**, and **intern feedback** flows.
- **Concrete:** keyboard path through primary actions, visible focus, form **labels**/errors, sufficient **contrast** for Bootstrap theme (verify theme tokens).

### Implementation considerations

- **Error pages:** friendly **4xx/5xx** Thymeleaf templates; **no** stack traces to users in production.
- **Degraded LLM:** global or page **banner** plus **mentor-only** scoring path (**User journeys**).
- **Native shell** and **CLI** are **out of scope** for this project type.

## Project scoping & phased development

**Authoritative feature phasing** is **Product scope** (MVP / Growth / Vision). This section states **strategy** and **risk** only—avoid duplicating the MVP bullet list.

### MVP strategy & philosophy

- **Approach:** **Problem-solving MVP** — smallest slice that proves **mentor-trusted** **AI draft → mentor publish** on **Git-backed** work, **local inference**, and **containerized** pilot deployment.
- **Team (indicative):** **1–2** engineers strong in **web apps**, **containers**, and **inference ops**; part-time mentor/program contact for rubric and pilot feedback.

### Risk mitigation strategy

| Category | Mitigation |
|----------|------------|
| **Technical** | Time-box inference integration; ship **mentor-only** fallback; **pin** model artifacts; automated checks for **version-control** failure modes |
| **Adoption** | Pilot with **≥3** mentors / **≥10** interns; measure time saved, **override** rate, qualitative trust |
| **Resource** | One **version-control** vendor and one stack before cutting **audit/publication** depth |

## Traceability & inputs

- **Brainstorming** (`_bmad-output/brainstorming/brainstorming-session-2026-04-08-070204.md`) informed **trust**, **draft vs publish**, **audit**, **data minimization to inference**, and **degraded inference**—reflected in **Success criteria**, **User journeys**, and **FR13–FR32**.

## Functional requirements

**Capability contract:** features not traceable here are **out of scope** unless the FR list is revised.

### Identity and access

- **FR1:** A user can **sign in** using **organization-provided** credentials.
- **FR2:** A user can **sign out** and end an authenticated session.
- **FR3:** The product can **enforce** that each action runs under **exactly one** authenticated identity.
- **FR4:** The product can **restrict** features by **role** (e.g. intern, mentor, administrator).

### User administration

- **FR5:** An **administrator** can **create**, **disable**, and **update** user accounts.
- **FR6:** An **administrator** can **assign** and **change** user **roles**.

### Tasks and assignments

- **FR7:** An **authorized user** (mentor or administrator, per policy) can **create** and **edit** **tasks** with instructions and due expectations.
- **FR8:** An **intern** can **view** tasks **assigned** to them.
- **FR9:** An **intern** can **attach** a **submission** to a task by supplying **version-control** coordinates (e.g. repository and revision).

### Source retrieval

- **FR10:** The system can **retrieve** source content for a submission from an external **version-control provider** using **organization-configured** credentials.
- **FR11:** The system can **record** retrieval **success**, **failure**, and **diagnostic** details **without** exposing secrets to end users.
- **FR12:** An **authorized user** can **re-trigger** retrieval or **correct** coordinates when retrieval fails (within policy).

**MVP implementation note (Examinai):** Retrieval targets a **GitHub REST v3–compatible** API (`GIT_PROVIDER_BASE_URL`, e.g. `https://api.github.com`). Interns supply **repository** (`owner/name`), **commit ref**, and a **required path scope** (repo-relative file path—no default file). The integration **first** calls **`GET /repos/{owner}/{repo}/commits/{ref}`** to load commit metadata and **`files[]`**, then resolves file text using, in order, the matching entry’s **`patch`**, **`raw_url`**, **`contents_url`**, or **`GET /repos/{owner}/{repo}/contents/{path}?ref={ref}`** when no matching **`files[]`** row exists. **Spring `RestClient`** performs HTTP; secrets are env-only (**FR31**). See **`docs/planning-artifacts/architecture.md`** (Git provider HTTP) and **`README.md`** for operator detail.

### Mentor review workspace

- **FR13:** A **mentor** can **view** a **list** of submissions **awaiting** or **in** review.
- **FR14:** A **mentor** can **open** a submission and **inspect** retrieved source **in context** of the task.
- **FR15:** A **mentor** can **enter** or **adjust** **structured** scores for **quality**, **readability**, and **correctness**.
- **FR16:** A **mentor** can **enter** **free-text** feedback **independent of** model-generated text.
- **FR17:** A **mentor** can **publish** a review so it becomes the **official** outcome for the intern, **superseding** draft assistance according to visibility rules.

### AI-assisted assessment

- **FR18:** The system can **request** an **AI-generated** **draft** assessment (scores and/or commentary) for a submission **subject to** policy.
- **FR19:** The system can **persist** AI draft outputs **separately** from **published** mentor outcomes.
- **FR20:** The system can **record** **metadata** for each AI invocation sufficient for **audit** (e.g. **which model/instance** and **when**).
- **FR21:** A **mentor** can **complete** a review **without** using AI assistance.

### Intern feedback and transparency

- **FR22:** An **intern** can **view** the **published** scores and mentor feedback for their submission.
- **FR23:** The product can **distinguish** **AI draft** material from **mentor-published** material **in the intern experience** so drafts are **not mistaken** for final grades.
- **FR24:** An **intern** can **view** the **status** of their submission through the review lifecycle (e.g. awaiting review, published).
- **FR25:** The product can **withhold** **other interns’** outcomes from a given intern **per** privacy policy.

### Audit and accountability

- **FR26:** A **coordinator** (or other **auditor** role) can **access** a **read-only** record of submission, retrieval, AI draft, and **published** mentor decision **for a case**.
- **FR27:** The system can **show** **who** published each official review and **when**.
- **FR28:** The system can **retain** historical versions of outcomes **sufficient** to explain changes across **resubmissions** (within retention policy).

### Degraded operation

- **FR29:** The system can **detect** inference **unavailability** or **timeouts** and **signal** a **degraded** mode.
- **FR30:** In degraded mode, a **mentor** can still **publish** a **mentor-only** official review.

### Configuration (MVP-level)

- **FR31:** An **administrator** (or deployment-time configuration, per org) can **configure** **version-control** access **parameters** **without** embedding secrets in user-visible fields.
- **FR32:** The product can **run** in a **split deployment** topology with **application**, **database**, and **inference** **runtimes** **independently replaceable** (capability: **support** that operational model for pilot).
