# Story 5.1: Request AI draft assessment via Spring AI

Status: ready-for-dev

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

- [ ] `integration.ai` service: build prompt or chat from **normalized source** already stored; apply max size / truncation rules.
- [ ] Config: model id, base URL, timeouts in `application-*.yml` + env.
- [ ] Controller action: POST trigger draft generation (mentor-only); delegate to service.
- [ ] Tests: mock `ChatClient` / Spring AI test doubles; verify timeout configuration present.

## Dev Notes

### Prerequisites

- **Epic 4** review context; **Epic 3** source available.
- Initializr **spring-ai-ollama** (or chosen module) from **1.1**.

### Architecture compliance

- LLM only via Spring AI; **`InferenceUnavailableException`** pattern for later **5.4** [Source: architecture **Process Patterns**].

### References

- [Source: `_bmad-output/planning-artifacts/epics.md` — Story 5.1]

## Dev Agent Record

### Agent Model Used
_(filled by dev agent)_
### Debug Log References
### Completion Notes List
### File List
_(filled by dev agent on completion)_

---

**Story completion status:** `ready-for-dev` — Ultimate context engine analysis completed.
