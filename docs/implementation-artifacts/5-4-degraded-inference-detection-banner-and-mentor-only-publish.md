# Story 5.4: Degraded inference detection, banner, and mentor-only publish

Status: done

<!-- Ultimate context engine analysis completed - comprehensive developer guide created. -->

## Story

As a **mentor**,
I want **clear signals when the model is unavailable and a path to publish anyway**,
so that **reviews do not stall on infrastructure issues**.

## Acceptance Criteria

1. **Degraded signal** — Given LLM down or requests time out,
   when mentor uses review workspace,
   then **degraded inference** banner or global flag explains situation (**FR29**, **UX-DR7**).

2. **Mentor-only publish** — In degraded mode,
   when publishing,
   then **4.4** mentor publish remains available **without** AI (**FR30**).

3. **Observability** — Degraded state observable per **NFR8** (health or UI flag—align with ops).

4. **Intern alignment** — Degraded labeling must not imply final model grade (**FR23**—exact intern copy may refine in **Epic 6**).

5. **Traceability** — **FR29**, **FR30**, **UX-DR7**, **NFR8**.

## Tasks / Subtasks

- [x] Detect timeouts / connection errors from **5.1**; set session or request attribute e.g. `degradedInference` (reuse name from architecture **Process Patterns — Loading / degraded paths**).
- [x] Shared Thymeleaf fragment: banner in layout for review pages.
- [x] Disable or soften “Generate draft” CTA when degraded; do not block publish.
- [x] Actuator/custom health optional: LLM probe behind feature flag.
- [x] Tests: simulate LLM failure—banner shown; publish still succeeds.

## Dev Notes

### Architecture compliance

- Use **consistent** model attribute / flash key namespace once introduced [Source: architecture **Process Patterns**].

### Current codebase (before 5.4)

- **5.1** failures raise `InferenceUnavailableException`; `TaskSubmissionMentorController.generateAiDraft` sets flash `reviewError` with a short message (no persistent degraded flag).
- **5.2** persists drafts **only** on successful inference; no partial/degraded row is written when the model is down.
- **4.4** publish remains available regardless of AI outcome—this story adds a visible **degraded** banner / shared fragment, softens the “Generate AI draft” CTA when appropriate, and optional health alignment (NFR8).

### References

- [Source: `_bmad-output/planning-artifacts/epics.md` — Story 5.4]

## Dev Agent Record

### Agent Model Used
Cursor agent (implementation)
### Debug Log References
### Completion Notes List
- Session flag `degradedInference` set on `InferenceUnavailableException` in `generateAiDraft`, cleared on successful draft; `DegradedInferenceModelAdvice` exposes it to mentor queue and task submission views.
- Shared fragment `tasks/fragments/degraded-inference-banner.html` on submission detail and review queue; AI draft form softened (opacity, tooltip, helper text) without disabling publish.
- Optional `OllamaLlmHealthIndicator` (bean `llmInference`) behind `examinai.ai.llm-health-probe.enabled` (default false); probes `GET {spring.ai.ollama.base-url}/api/tags`.
- Tests: WebMvc session assertions, queue banner content, full integration publish after mocked AI failure, health indicator DOWN to bad port when probe enabled.

### File List
- `src/main/java/com/examinai/app/web/DegradedInferenceAttributes.java`
- `src/main/java/com/examinai/app/web/DegradedInferenceModelAdvice.java`
- `src/main/java/com/examinai/app/web/task/TaskSubmissionMentorController.java`
- `src/main/java/com/examinai/app/integration/ai/LlmHealthProbeProperties.java`
- `src/main/java/com/examinai/app/integration/ai/OllamaLlmHealthIndicator.java`
- `src/main/java/com/examinai/app/integration/ai/AiIntegrationConfiguration.java`
- `src/main/resources/application.yml`
- `src/main/resources/templates/tasks/fragments/degraded-inference-banner.html`
- `src/main/resources/templates/tasks/submission-detail.html`
- `src/main/resources/templates/review/queue.html`
- `src/test/java/com/examinai/app/web/task/TaskSubmissionMentorAiDraftWebMvcTest.java`
- `src/test/java/com/examinai/app/web/review/MentorReviewQueueWebMvcTest.java`
- `src/test/java/com/examinai/app/web/DegradedInferenceMentorIntegrationTest.java`
- `src/test/java/com/examinai/app/integration/ai/OllamaLlmHealthIndicatorIntegrationTest.java`
- `_bmad-output/implementation-artifacts/sprint-status.yaml`

---

**Story completion status:** `done`.
