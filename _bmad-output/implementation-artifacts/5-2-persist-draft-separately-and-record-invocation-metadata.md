# Story 5.2: Persist draft separately and record invocation metadata

Status: done

<!-- Ultimate context engine analysis completed - comprehensive developer guide created. -->

## Story

As the **system**,
I want **to store AI output and model metadata apart from published mentor rows**,
so that **audits can answer what the model said and when**.

## Acceptance Criteria

1. **Separate persistence** — Given successful model response,
   when results save,
   then draft scores/text live in **distinct** storage from **published** mentor review (**FR19**).

2. **Audit metadata** — Each invocation records **model identity/version**, **timestamp**, optional **prompt hash** if implemented (**FR20**).

3. **Mentor labeling** — When draft exists on review screen,
   then UI labels it **not final** vs published fields (**UX-DR4**).

4. **Traceability** — **FR19**, **FR20**, **UX-DR4**.

### AC → implementation (verification)

| AC | Evidence |
|----|----------|
| FR19 | Liquibase tables **`ai_drafts`** and **`model_invocations`** (FK to `submissions` only). `MentorReviewService.publish` writes `published_reviews` and does not touch AI tables. |
| FR20 | `model_invocations`: `invoked_at`, `model_name`, optional `model_version`, optional `prompt_hash` (SHA-256 of payload from `AiDraftPayloadLoader`, not raw prompt). |
| UX-DR4 | `submission-detail.html`: warning panel, **Not final** badge, copy vs official rubric/publish. Model attr: `latestAiDraft` (`AiDraftView`). |
| Traceability | Integration: `Epic5AiDraftPersistenceIntegrationTest`; controller: `TaskSubmissionMentorAiDraftWebMvcTest`. |

## Tasks / Subtasks

- [x] Liquibase: tables **`ai_drafts`** / **`model_invocations`** (FR19)—FK to submission context; **never** overwrite published columns.
- [x] Transactional service: persist draft + metadata after **5.1** returns (`AiDraftPersistenceService.persistSuccessfulDraft`).
- [x] Thymeleaf: draft panel with explicit **non-final** styling (`latestAiDraft`).
- [x] Tests: draft row exists; published row unchanged until **4.4** publish.

## Dev Notes

### Architecture compliance

- **AI draft rows** vs **published review rows** remain distinguishable [Source: architecture **Data boundaries**].
- **Never** log full LLM payloads with secrets (**NFR12**). Stored **hash** only for audit, not full user payload.

### References

- [Source: `_bmad-output/planning-artifacts/epics.md` — Story 5.2]

### Follow-on stories

- **5.3** — Optional AI / no dependence on draft for publish (publish already independent; may add explicit tests).
- **5.4** — Degraded inference banner (today: flash `reviewError` only on AI failure).

## Change log

- **2026-04-08** — Initial implementation: Liquibase `006`, `AiDraftPersistenceService`, `AiModelAuditDescriptor`, mentor detail UI.
- **2026-04-09** — Artifact refresh: AC verification table, actual table names, follow-on pointers, NFR12 hash clarification.
- **2026-04-09** — Story marked **done** (code review approved); sprint status updated.

## Dev Agent Record

### Agent Model Used
Auto (Cursor)
### Debug Log References
_(none)_
### Completion Notes List
- Liquibase `006-epic5-ai-draft-persistence.yaml` (changesets `006-01-model-invocations`, `006-02-ai-drafts`): audit row per success + 1:1 draft text; cascade on submission delete.
- After `AiDraftAssessmentService.generateDraft` succeeds, `TaskSubmissionMentorController` calls `persistSuccessfulDraft`; inference stays outside `@Transactional` in 5.1 service; persistence is a short transaction.
- AI draft is no longer carried only in flash attributes—full text loads from DB on GET.
- Env/config: `EXAMINAI_AI_AUDIT_MODEL_VERSION` / `examinai.ai.draft-assessment.audit-model-version` for optional version tag alongside Ollama model name.
### File List
- src/main/resources/db/changelog/db.changelog-master.yaml
- src/main/resources/db/changelog/changes/006-epic5-ai-draft-persistence.yaml
- src/main/resources/application.yml
- src/main/resources/templates/tasks/submission-detail.html
- src/main/java/com/examinai/app/domain/ai/AiDraft.java
- src/main/java/com/examinai/app/domain/ai/AiDraftRepository.java
- src/main/java/com/examinai/app/domain/ai/ModelInvocation.java
- src/main/java/com/examinai/app/domain/ai/ModelInvocationRepository.java
- src/main/java/com/examinai/app/integration/ai/AiDraftAssessmentProperties.java
- src/main/java/com/examinai/app/integration/ai/AiModelAuditDescriptor.java
- src/main/java/com/examinai/app/service/AiDraftPersistenceService.java
- src/main/java/com/examinai/app/service/AiDraftView.java
- src/main/java/com/examinai/app/web/task/TaskSubmissionMentorController.java
- src/test/java/com/examinai/app/web/Epic5AiDraftPersistenceIntegrationTest.java
- src/test/java/com/examinai/app/web/task/TaskSubmissionMentorAiDraftWebMvcTest.java
- src/test/java/com/examinai/app/config/AiDraftAssessmentPropertiesBindingTest.java

---

**Story completion status:** `done` — accepted 2026-04-09.
