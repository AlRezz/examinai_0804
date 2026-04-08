---
stepsCompleted: [1, 2]
inputDocuments: []
session_topic: 'AI-assisted mentor–intern code review: score quality, readability, and correctness; Spring + Spring AI; local LLM (deepseek-r1:8b); app, database, and LLM each in separate Docker images'
session_goals: 'Generate diverse ideas for product features, mentor/intern workflows, scoring and rubrics, trust/safety, and architecture/integration—within the stated stack and deployment constraints'
selected_approach: 'ai-recommended'
techniques_used: ['Question Storming', 'Six Thinking Hats', 'Role Playing']
ideas_generated: []
context_file: ''
current_technique: ''
role_playing_cast: ['mentor', 'intern']
role_playing_note: 'Both role monologues skipped in chat; facilitator synthesis only.'
brainstorming_techniques_round_complete: true
question_storming: 'partial — transitioned early to Six Hats at user request'
six_hats_progress: ['White', 'Red', 'Yellow', 'Black', 'Green', 'Blue']
six_hats_complete: true
---

# Brainstorming Session Results

**Facilitator:** Alex
**Date:** Wednesday, April 8, 2026

## Session Overview

**Topic:** AI-assisted mentor–intern code review: score quality, readability, and correctness; Spring + Spring AI; local LLM (deepseek-r1:8b); app, database, and LLM each in separate Docker images.

**Goals:** Generate diverse ideas for product features, mentor/intern workflows, scoring and rubrics, trust/safety, and architecture/integration—within the stated stack and deployment constraints.

### Context Guidance

_No optional context file was attached; topic and technical constraints came from live session setup._

### Session Setup

Session confirmed. Facilitation will stay in generative mode (volume and variety before sorting). Ideas will deliberately rotate across product, UX, mentor workflow, scoring philosophy, model limits, security/privacy, and operations so we avoid one-track “only API design” thinking.

## Technique Selection

**Approach:** AI-recommended techniques.

**Recommended sequence**

- **Question Storming** — Clarify the problem and trust surface before solution sketching.
- **Six Thinking Hats** — Sweep facts, risks, benefits, emotion, creativity, and process.
- **Role Playing** — Stress-test workflows from mentor, intern, and org perspectives.

**AI rationale:** The topic mixes human judgment, LLM behavior, and containerized deployment; framing questions first reduces building the wrong rubric; structured hats prevents blind spots; roles ground features in real handoffs.

### Technique execution log

#### Question Storming — partial wrap-up

**Status:** User chose to move on before exhausting the question list; valuable breadth was already captured.

**Clusters emerging from batches A–B:** mentor–model disagreement and score invalidation; data boundary into LLM container; accountability; readability vs style; confident-wrong model + intern UX; product identity (who is “customer”); review unit and lifecycle; visibility of model vs mentor; failure when LLM unavailable; retention/audit/model versioning; Spring AI trust/timeout; irrevocable human override.

**Batch A — Alex (trust / scoring / model behavior)**

- What would invalidate a score as “correct” if the mentor disagreed with the model?
- What must never leave the intern’s repo or the mentor’s review into the LLM container?
- Who is legally or ethically “accountable” when the model flags code as “correct” but it’s wrong in production?
- How do you know you’re measuring readability vs preference (style wars)?
- What happens when deepseek-r1:8b is wrong but sounds confident—what does the UI owe the intern?

**Batch B — Facilitator pivot (product / UX / workflow / ops / integration)**

- Is the primary customer the mentor, the intern, or the organization—and who pays in time vs money?
- What does “done” look like for one review cycle: a grade, a conversation, a badge, a merge gate?
- Do interns see raw model output, mentor-only notes, or both—and when?
- How does the system handle iterative commits: re-score everything or deltas only?
- What’s the smallest unit of review: file, PR, function, commit, “assignment milestone”?
- Should mentors calibrate rubrics per cohort, per language, or globally—and who approves changes?
- What languages and frameworks must v1 support—and does the model get different prompts per stack?
- How do you stop the product from becoming a “plagiarism detector with extra steps”?
- What happens offline or when the LLM container is down: block reviews, queue, or mentor-only mode?
- Who can see history: can interns compare scores across peers, or is that forbidden?
- What retention policy applies to prompts, responses, and source code snippets in the DB?
- How do you version the model image and prove which weights produced a given score for an audit?
- What SLAs matter: time-to-first-suggestion, mentor edit time, intern confusion rate?
- How does Spring AI timeout/retry behavior change what users trust?
- Is there a “human override” artifact that must be irrevocable in the database—and who can delete it?

#### Six Thinking Hats — in progress

**Order:** White → Red → Yellow → Black → Green → Blue (process close).

**White Hat (facts / knowns)** — *living list; updated from user clarifications.*

**Stated in this workshop**

- Product intent: an **AI-assisted** application where **mentors** review **interns’** code and are helped to **score** **quality**, **readability**, and **correctness**.
- **Spring Framework** is the intended application stack.
- **Spring AI** is the intended integration layer for **LLM** communication.
- LLM runtime is **local**; user referenced **Llama** with model **deepseek-r1:8b**, supplied as a **Docker image**.
- **Three** deployment units in **separate Docker images**: **application**, **database**, **LLM** (each not in the same image as the others).
- **Java** language level: **21+**.
- **Database:** **PostgreSQL**; **Liquibase** for **schema versioning** (migrations).
- **Code ingestion:** fetched from **Git via API** (hosting product / API shape not pinned).
- **Security:** **Spring Security** with **basic username/password** authentication; **user credentials / accounts stored in the database** (not SSO/OAuth in current scope).
- **Persistence:** **Hibernate** (JPA); domain to store **sources**, **mentors**, **tasks**, and related entities (user indicated a broad, extensible model).
- **Runtime orchestration:** **Docker Compose** (assumes multi-service local/deploy story consistent with separate images).
- **UX / frontend (stated):** simple **server-rendered** UI using **Thymeleaf**, **jQuery**, and **Bootstrap**; user also mentioned **JSP**—in typical Spring MVC apps one primary template stack is standard; **hybrid or final choice** to confirm in build (avoid duplicating two full view layers without intent).

**From project config (BMad)**

- Project folder name placeholder: **examinai_0804**; output/docs paths exist for planning artifacts.

**Explicit unknowns (still open)**

- **Spring Boot** version (Java 21+ is set; Boot line not fixed).
- **Git provider** and API (GitHub, GitLab, self-hosted, etc.) + auth to Git (token, app, SSH proxy).
- Exact **LLM serving** stack in the LLM image (Ollama, llama.cpp server, other) and wire format Spring AI will use.
- **Compose** service topology details (networks, volumes, healthchecks, secrets).
- What exactly is stored beyond named aggregates: **prompts**, **raw model output**, **scores**, **rubric versions**, **audit**—and **retention**/PII policy.
- **Multi-tenant** or single-org assumption; admin role vs mentor/intern only; password policies, reset flows, and credential storage details (hashing algorithm, pepper, etc.) not pinned.

**Red Hat (feelings / intuition)** — *gut reactions only; no evidence or defense required.*

- *(No explicit red-hat bullets captured — user advanced with “next hat.”)*

**Yellow Hat (benefits / optimism)** — *pluses and upside only; best believable cases.*

- *(No explicit yellow-hat bullets captured — user advanced with “next hat.”)*

**Black Hat (risks / caution)** — *what could go wrong, weaknesses, threats — no solutions yet.*

- *(No dedicated black-hat list captured in chat; user advanced with “next hat.” A White Hat refinement was noted meanwhile: DB-backed basic auth.)*

**Green Hat (creativity / alternatives)** — *new ideas, options, mitigations, “what if” constructions — building allowed.*

- UX/presentation choice stated in chat: **Thymeleaf**, **jQuery**, **Bootstrap** (plus **JSP** mention — reconciled under White Hat as implementation detail).

**Blue Hat (process / meta — close of Six Hats)**

- **Coverage:** White Hat is the richest leg (stack, auth, Git, DB, Compose, UI). Red/Yellow/Black had little explicit chat capture; Green yielded one concrete UI stack addition.
- **Working style:** User repeatedly advanced with “next hat” — optimize future sessions for **faster capture** (optional: one combined “retro” pass) or accept **documentation-first** clarifications mid-sequence as we did for auth and UI.
- **Substantive thread from Question Storming still open:** trust, mentor–model disagreement, data sent to LLM, audit/model versioning — carry into PRD/architecture or **Role Playing**.
- **Suggested flow:** Either run **Role Playing** (mentor / intern only — user confirmed) next, or exit brainstorming into **idea organization** / PRD / architecture per BMad pipeline.

#### Role Playing — complete (light touch; cast: mentor + intern)

**Shared scenario (baseline):** An **intern** has a **task** tied to a repo branch/commit retrieved **via Git API**. The **local LLM** suggests scores and commentary for **quality**, **readability**, and **correctness**. The **mentor** uses the **web app** (Thymeleaf/Bootstrap) with **DB-backed login** to review and finalize.

**Mentor voice** — *(capture below from dialogue)*

- *(Skipped in chat — user sent “next” before a mentor monologue.)*

**Intern voice** — *(capture below from dialogue)*

- *(Skipped in chat — user sent “next” before an intern monologue.)*

**Tensions / handoff insights** — *(facilitator synthesis from scenario + earlier Question Storming / White Hat — not from first-person role play)*

- **Final authority:** Mentors need a clear **“mine vs model’s”** boundary—**override**, **comment**, and **published score** should be unambiguous in the UI and in persistence (ties to Question Storming on **disagreement** and **invalidating** a score).
- **Intern trust:** Interns need **transparent criteria** and **safe language** from the model; confident-wrong output must not read as **final** (intern-facing copy vs mentor-facing).
- **Visibility timing:** Tension on whether interns see **raw LLM output** before the mentor acts—default assumption: **mentor-reviewed or clearly labeled “AI draft”** unless policy says otherwise.
- **Data to LLM:** Handoff from **Git API** to LLM container must respect **minimum necessary** source and **no secrets** (tokens, env) in prompts—aligns with “what must never leave…” questions.
- **Workflow product ideas implied:** Mentor dashboard (queue, diff, model rationale panel); intern view (task, rubric, optional revision loop); **audit** row: model version + prompt hash + final mentor scores.
