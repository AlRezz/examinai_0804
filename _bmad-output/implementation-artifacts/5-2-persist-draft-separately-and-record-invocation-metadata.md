# Story 5.2: Persist draft separately and record invocation metadata

Status: ready-for-dev

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

## Tasks / Subtasks

- [ ] Liquibase: `ai_draft` / `model_invocation` tables (names illustrative)—FK to submission/review context; **never** overwrite published columns.
- [ ] Transactional service: persist draft + metadata after **5.1** returns.
- [ ] Thymeleaf: draft panel with explicit **non-final** styling.
- [ ] Tests: draft row exists; published row unchanged until **4.4** publish.

## Dev Notes

### Architecture compliance

- **AI draft rows** vs **published review rows** remain distinguishable [Source: architecture **Data boundaries**].
- **Never** log full LLM payloads with secrets (**NFR12**).

### References

- [Source: `_bmad-output/planning-artifacts/epics.md` — Story 5.2]

## Dev Agent Record

### Agent Model Used
_(filled by dev agent)_
### Debug Log References
### Completion Notes List
### File List
_(filled by dev agent on completion)_

---

**Story completion status:** `ready-for-dev` — Ultimate context engine analysis completed.
