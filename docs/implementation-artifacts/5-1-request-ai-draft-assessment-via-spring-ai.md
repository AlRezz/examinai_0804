# Story 5.1: Request AI draft assessment via Spring AI

Status: done

<!-- Ultimate context engine analysis completed - comprehensive developer guide created. -->

## Story

As a **mentor**,
I want **to request an AI draft assessment for the current submission**,
so that **I get assistive signals without ceding final judgment**.

## Acceptance Criteria

1. **Spring AI call** — Given submission with retrievable source within policy,
   when mentor triggers **Generate AI draft**,
   then app invokes **Spring AI** (e.g. Ollama starter) with **bounded timeout/retry** (**NFR4**).

2. **Payload safety** — Request payload **excludes** secrets, env files, tokens; follows **data-minimization** / pilot policy documented in README (**NFR7**).

3. **Boundary** — No ad-hoc HTTP to LLM from controllers—only **`integration.ai`** facade [Source: architecture].

4. **Traceability** — **FR18**, **NFR4**, **NFR7**.

## Tasks / Subtasks

- [x] `integration.ai` service: build prompt or chat from **normalized source** already stored; apply max size / truncation rules.
- [x] Config: model id, base URL, timeouts in `application-*.yml` + env.
- [x] Controller action: POST trigger draft generation (mentor-only); delegate to service.
- [x] Tests: mock `ChatClient` / Spring AI test doubles; verify timeout configuration present.
- [x] **Correct-course (2026-04-08):** transaction split, flash cap, missing-submission handling, validated properties, executor bean, timeout test — see Sprint Change Proposal and Senior Review action items (all addressed in code).
- [x] **Second CR follow-ups:** overall inference wall budget, bounded error flash text, Thymeleaf `trim` guard vs `hasText`, `request-timeout-seconds` ≥ 1, README env for wall cap.

## Dev Notes

### Prerequisites

- **Epic 4** review context; **Epic 3** source available.
- Initializr **spring-ai-ollama** (or chosen module) from **1.1**.

### Architecture compliance

- LLM only via Spring AI; **`InferenceUnavailableException`** pattern for later **5.4** [Source: architecture **Process Patterns**].

### References

- [Source: `_bmad-output/planning-artifacts/epics.md` — Story 5.1]

## Correct course (Sprint change)

**Proposal:** [`_bmad-output/planning-artifacts/sprint-change-proposal-2026-04-08.md`](../planning-artifacts/sprint-change-proposal-2026-04-08.md)  
**Scope classification:** **Minor** — no PRD/epic scope change; in-story engineering fixes only.  
**Implementation:** Payload assembly moved to **`AiDraftPayloadLoader`** (`@Transactional(readOnly = true)`); **`AiDraftAssessmentService`** runs inference outside any DB transaction. **`aiDraftExecutor`** Spring bean (`close` on shutdown). Controller: **`NoSuchElementException`**, **`max-flash-chars`** truncation. Properties: **`@Validated`** / **`@Min`/`@Max`**. Tests: **`AiDraftPayloadLoaderTest`**, timeout path in **`AiDraftAssessmentServiceTest`**.

## Dev Agent Record

### Agent Model Used
Composer (Cursor agent)

### Debug Log References
— 

### Completion Notes List
- Added `com.examinai.app.integration.ai` (`AiDraftAssessmentService`, `InferenceUnavailableException`, `AiDraftAssessmentProperties`, `ChatClient` bean).
- Mentor POST `/tasks/{taskId}/submissions/{internId}/ai-draft-assessment` + **Generate AI draft** on submission detail; flash display for assistive text.
- Bounded inference: per-call timeout (`Future.get`), configurable retries/backoff; prompt uses task brief + truncated `gitRetrievedText` only (no repo URL/token).
- README pilot section for NFR7 / env vars; `application.yml`: `spring.ai.ollama` (base URL, model, init), `examinai.ai.draft-assessment.*`.
- Tests: `AiDraftAssessmentServiceTest`, `AiDraftPayloadLoaderTest`, `AiDraftAssessmentPropertiesBindingTest`, `TaskSubmissionMentorAiDraftWebMvcTest`.
- **Post-review:** `AiDraftPayloadLoader`, executor bean, flash cap + env `EXAMINAI_AI_DRAFT_MAX_FLASH_CHARS`, controller hardening, validated properties.
- **Second CR:** `max-inference-wall-seconds` (default 300) caps total inference wall time; per-attempt timeout trimmed to remaining budget; default per-attempt timeout 90s; `flashSafeMessage` for `InferenceUnavailableException`; Thymeleaf uses `#strings.trim(gitRetrievedText)` for button visibility; `@Min(1)` on `requestTimeoutSeconds`; safer `ExecutionException` detail when message null.

### File List
- `src/main/java/com/examinai/app/integration/ai/AiDraftAssessmentService.java`
- `src/main/java/com/examinai/app/integration/ai/AiDraftPayloadLoader.java`
- `src/main/java/com/examinai/app/integration/ai/AiDraftAssessmentProperties.java`
- `src/main/java/com/examinai/app/integration/ai/AiIntegrationConfiguration.java`
- `src/main/java/com/examinai/app/integration/ai/InferenceUnavailableException.java`
- `src/main/java/com/examinai/app/web/task/TaskSubmissionMentorController.java`
- `src/main/resources/templates/tasks/submission-detail.html`
- `src/main/resources/application.yml`
- `README.md`
- `src/test/java/com/examinai/app/integration/ai/AiDraftAssessmentServiceTest.java`
- `src/test/java/com/examinai/app/integration/ai/AiDraftPayloadLoaderTest.java`
- `src/test/java/com/examinai/app/config/AiDraftAssessmentPropertiesBindingTest.java`
- `src/test/java/com/examinai/app/web/task/TaskSubmissionMentorAiDraftWebMvcTest.java`
- `_bmad-output/planning-artifacts/sprint-change-proposal-2026-04-08.md`
- `_bmad-output/implementation-artifacts/sprint-status.yaml`
- `docs/implementation-artifacts/sprint-status.yaml`

## Change Log
- 2026-04-08: Implemented Spring AI draft assessment (story 5.1); sprint status → review.
- 2026-04-08: Senior Developer Review (AI) recorded below.
- 2026-04-08: Correct Course workflow — Sprint Change Proposal + engineering fixes for review findings.
- 2026-04-08: Second Senior Developer Review (AI) recorded below.
- 2026-04-08: Dev story pass — second CR action items implemented; tests green.
- 2026-04-08: Story and sprint status set to **done**; review outcomes finalized.

---

## Senior Developer Review (AI)

**Reviewer:** Code review workflow (adversarial layers: acceptance, blind paths, edge cases)  
**Date:** 2026-04-08  
**Outcome:** **Approve** — findings addressed in code (2026-04-08); story closed after second CR pass.

### Summary

The feature meets the story’s intent (Spring AI behind `integration.ai`, minimized prompt, mentor-only trigger, tests). The main gap is **holding a read-only JPA transaction open for the full LLM duration**, which risks pool exhaustion and is inconsistent with NFR4 operability under load. Secondary gaps: unhandled `NoSuchElementException` if the submission disappears between controller and service, and weak bounds on flash-payload size for very long model outputs.

### Acceptance criteria check

| AC | Verdict | Notes |
|----|---------|--------|
| AC1 Spring AI + timeout/retry | **Pass** (post-fix) | Timeout + retries; **inference outside DB transaction** via `AiDraftPayloadLoader` + non-transactional service. |
| AC2 Payload / NFR7 | **Pass** | Prompt omits repo/coordinates; README documents policy. Retrieved file *content* can still contain secrets if scope pointed at sensitive files—operational, not a new bypass. |
| AC3 Boundary | **Pass** | Controller delegates to `AiDraftAssessmentService` only. |
| AC4 Traceability | **Pass** | README + property/env surface (`EXAMINAI_AI_DRAFT_MAX_FLASH_CHARS`, `EXAMINAI_AI_DRAFT_MAX_INFERENCE_WALL_SECONDS`, etc.). |

### Action items (by severity)

- [x] **High** — Refactor `AiDraftAssessmentService.generateDraft` so the **Ollama / `ChatClient` call runs outside** a `@Transactional` boundary (e.g. split load-with-transaction vs inference-without, or `TransactionTemplate` for a short read only).
- [x] **Medium** — Handle **missing submission** after redirect (e.g. catch `NoSuchElementException` in controller or return explicit 404 / flash) instead of uncaught 500.
- [x] **Medium** — Cap **flash / displayed draft length** (truncate with notice) or persist draft in **5.2** before showing—avoid huge session attributes.
- [x] **Low** — Validate `examinai.ai.draft-assessment.*` (**e.g. `maxSourceChars` ≥ 1**, non-negative timeouts/retries) via `@Validated` / `@Min` on `AiDraftAssessmentProperties`.
- [x] **Low** — **Executor lifecycle:** register `ExecutorService` as a Spring bean with `DisposableBean`/`@PreDestroy` shutdown, or avoid a static per-service executor pattern for consistency with other integrations.
- [x] **Low** — **Tests:** add a focused test that **timeout path** fires (e.g. stub delayed future or property with 0s timeout)—task checkbox “verify timeout” is only partially satisfied by property binding today.

### Review Follow-ups (AI)

Addressed in codebase 2026-04-08.

---

## Second Senior Developer Review (AI)

**Reviewer:** Code review workflow (second pass)  
**Date:** 2026-04-08  
**Outcome:** **Approve** — action items addressed in code (2026-04-08)

### Findings (historical — as filed before fixes)

- **Medium** — Synchronous request thread risk with long cumulative timeouts.
- **Medium** — Unbounded error detail in flash for `InferenceUnavailableException`.
- **Low** — UI `isEmpty` vs backend `hasText` for `gitRetrievedText`.
- **Low** — `requestTimeoutSeconds` allowed `0`.

**Resolution:** `max-inference-wall-seconds` + per-attempt timeout capped to remaining budget; default per-attempt timeout **90s**; `flashSafeMessage`; Thymeleaf `#strings.trim` on retrieved text; `@Min(1)` on request timeout.

### Action Items

- [x] Add an overall request-time budget or move draft generation off the synchronous servlet path.
- [x] Truncate or normalize AI error messages before placing them in flash/session state.
- [x] Align the template guard with backend `hasText(...)` semantics.
- [x] Tighten timeout property validation to `>= 1` unless `0` is intentionally supported/documented.

### Residual Risk

Synchronous servlet work remains (acceptable for story 5.1; async/job could be a later enhancement). **Very large task title/description** in the prompt is not separately capped (operational / optional follow-up).

---

**Story completion status:** `done` — ACs satisfied; reviews closed; `./mvnw test` green in dev session.
