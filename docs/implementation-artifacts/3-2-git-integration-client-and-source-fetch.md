# Story 3.2: Git integration client and source fetch

Status: done

<!-- Ultimate context engine analysis completed - comprehensive developer guide created. -->

## Story

As the **system**,
I want **to retrieve normalized source text (or scoped files) for a submission using one HTTP client style**,
so that **mentors can review real code in Epic 4**.

## Acceptance Criteria

1. **Successful fetch** — Given a submission with valid coordinates and working credentials (**3.1**),
   when retrieval runs,
   then retrieved content or **metadata + normalized text** is stored per **data-minimization** (no raw blob explosion in DB if avoidable) (**FR10**).

2. **SLA / timeout** — Given typical pilot-sized scope,
   when retrieval runs,
   then completion aligns with **NFR3** (under **60 s P95** target) or returns a **controlled failure** (timeout), not a hung thread.

3. **Retries bounded** — Given transient/rate errors,
   when retries apply,
   then policy is **bounded** (count + backoff); request thread does not block indefinitely.

4. **Single client style** — **WebClient** **or** **RestClient**—**one** only; document in `GitClientConfig` / README (**architecture** enforcement).

5. **Traceability** — **FR10**, **NFR3**.

## Tasks / Subtasks

- [ ] Implement `integration.git.GitSourceClient` (name illustrative) using chosen client; inject **3.1** properties.
- [ ] Liquibase + entities for retrieval snapshot metadata / text storage per team decision.
- [ ] Service method: given `submissionId`, fetch and persist outcome; map provider errors to **typed** integration exceptions (safe messages later in **3.3**).
- [ ] Tests: contract/integration test with **WireMock** or Testcontainers **if** feasible; else document manual verification with stub provider.

## Dev Notes

### Prerequisites

- **3.1** configuration; **2.4** submissions.

### Architecture compliance

- All Git HTTP from **`integration.git`** [Source: architecture **Architectural Boundaries**].
- Never return raw provider exceptions to Thymeleaf—translate in **`@ControllerAdvice`** (may land in **3.3** with UX).

### References

- [Source: `_bmad-output/planning-artifacts/epics.md` — Story 3.2]
- [Source: `_bmad-output/planning-artifacts/architecture.md` — API & Communication Patterns]

## Dev Agent Record

### Agent Model Used
_(filled by dev agent)_
### Debug Log References
### Completion Notes List
### File List
_(filled by dev agent on completion)_

---

**Story completion status:** `done` — Ultimate context engine analysis completed.
