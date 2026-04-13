# Sprint Change Proposal — 2026-04-08

**Project:** examinai_0804  
**Trigger:** Senior Developer Review (**Changes Requested**) on Story **5.1** — AI draft assessment via Spring AI  
**Mode:** Batch (single consolidated proposal)  
**Classification:** **Minor** — in-story fixes and clarifications; no epic/PRD scope change  

---

## 1. Issue summary

Post-implementation code review found **no product intent mismatch** with Story 5.1, but identified **operational and robustness gaps**:

1. **Long-lived read-only DB transaction** — `generateDraft` kept a `@Transactional(readOnly = true)` span open for the entire LLM call (timeouts × retries), risking connection pool pressure and conflicting with NFR4 expectations.
2. **Unhandled `NoSuchElementException`** — if the submission vanished between controller load and service `findById`, the user could see an unhandled 500.
3. **Unbounded flash payload** — large model outputs stored in flash attributes could inflate HTTP session size.
4. **Secondary** — configuration validation, executor lifecycle, and an explicit **timeout-path** test were underspecified vs review bar.

**Evidence:** Code review recorded in `_bmad-output/implementation-artifacts/5-1-request-ai-draft-assessment-via-spring-ai.md` (Senior Developer Review section).

---

## 2. Impact analysis

### Epic impact

| Epic | Affected? | Notes |
|------|-----------|--------|
| **Epic 5** (AI-assisted drafts) | Yes (implementation only) | Story 5.1 acceptance criteria unchanged; delivery quality improved. |
| Other epics | No | |

**Epic 5 can still complete as planned.** No epic scope or ordering change.

### Story impact

| Story | Change |
|-------|--------|
| **5.1** | Remains owner of AI draft request; add corrective implementation + close review items. Status may move **review → in-progress → review** during fixes. |
| **5.2** | Unchanged intent; optional note: **persisted** draft (5.2) will replace flash as system-of-record—flash cap remains a bridge until then. |
| **5.3–5.4** | No change. |

### Artifact conflicts (PRD / architecture / UX)

| Artifact | Update required? |
|----------|------------------|
| PRD | **No** — NFR4/NFR7 already align. |
| Architecture (`integration.ai`, Spring AI) | **No** — pattern unchanged; clarification that **inference runs outside DB transactions** strengthens alignment. |
| UX | **No** — same mentor flow; possible short truncation notice in UI text if draft exceeds flash cap (copy only). |

### Technical impact

- **Code:** `integration.ai` refactor (transaction boundary), controller handling, properties, executor bean, tests.
- **Config:** Optional `examinai.ai.draft-assessment.max-flash-chars` (env-driven).
- **Infra / deploy:** None.

---

## 3. Recommended approach

**Direct adjustment** (within Story 5.1):

1. Split **read** (short `@Transactional(readOnly = true)`) from **inference** (non-transactional), via a dedicated loader bean or equivalent.
2. Harden controller: **`NoSuchElementException`**, **flash truncation** with user-visible notice.
3. Add **validated** bounds on draft-assessment properties where practical.
4. Register **ExecutorService** as a Spring bean with **clean shutdown**.
5. Add a **timeout-path** unit test.

**Not recommended:** Rolling back 5.1 or splitting a new epic — effort and risk outweigh benefit.

**Effort:** ~0.5–1 dev day. **Risk:** Low (localized changes, existing tests extended).

---

## 4. Detailed change proposals

### 4.1 Story 5.1 (`5-1-request-ai-draft-assessment-via-spring-ai.md`)

**Section: Senior Developer Review → Action items**

- Track each action item to **Done** in the story file as code merges.
- After all High/Medium items close, set review outcome to **Approve** or remove **Changes Requested** per team norm.

**Section: Tasks / Subtasks (optional add)**

Add explicit corrective tasks (or rely on Review action items):

- Corrective: Transaction boundary (loader vs inference).
- Corrective: Controller + flash cap + missing entity handling.
- Corrective: Property validation, executor bean, timeout test.

_(Exact checkboxes updated in story file in repo.)_

### 4.2 PRD / Epics / Architecture

**No textual edits required** for this correction.

### 4.3 Configuration

**`application.yml`** (excerpt pattern):

```yaml
examinai:
  ai:
    draft-assessment:
      # ... existing ...
      max-flash-chars: ${EXAMINAI_AI_DRAFT_MAX_FLASH_CHARS:32768}
```

---

## 5. Implementation handoff

| Field | Value |
|-------|--------|
| **Scope** | **Minor** |
| **Owner** | Development (same team that implemented 5.1) |
| **Deliverables** | Merged PR: loader + service + controller + properties + tests; story review items checked; `./mvnw test` green |
| **Success criteria** | No transaction open during Ollama call; no unhandled 500 for missing submission on draft POST; flash bounded; timeout test passes |

**PO/SM:** Optional — keep **5.1** in sprint until review re-passed; no backlog reorder.

---

## 6. Checklist execution summary

| Section | Status |
|---------|--------|
| 1. Trigger & context | [x] Done — Story 5.1; technical limitation from review |
| 2. Epic impact | [x] Done — Epic 5 unchanged at scope level |
| 3. Artifact conflicts | [x] Done — No PRD/arch/UX edits |
| 4. … (remaining checklist) | [N/A] or [x] — no new epics, no pivot |

---

*Generated by Correct Course workflow. Next: implement Section 3 in codebase and close review action items on Story 5.1.*
