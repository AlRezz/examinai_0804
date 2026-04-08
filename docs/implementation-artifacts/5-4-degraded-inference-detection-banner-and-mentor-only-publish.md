# Story 5.4: Degraded inference detection, banner, and mentor-only publish

Status: ready-for-dev

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

- [ ] Detect timeouts / connection errors from **5.1**; set session or request attribute e.g. `degradedInference` (reuse name from architecture **Process Patterns — Loading / degraded paths**).
- [ ] Shared Thymeleaf fragment: banner in layout for review pages.
- [ ] Disable or soften “Generate draft” CTA when degraded; do not block publish.
- [ ] Actuator/custom health optional: LLM probe behind feature flag.
- [ ] Tests: simulate LLM failure—banner shown; publish still succeeds.

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
_(filled by dev agent)_
### Debug Log References
### Completion Notes List
### File List
_(filled by dev agent on completion)_

---

**Story completion status:** `ready-for-dev` — Ultimate context engine analysis completed.
