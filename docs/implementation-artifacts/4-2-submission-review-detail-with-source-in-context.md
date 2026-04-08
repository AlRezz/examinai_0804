# Story 4.2: Submission review detail with source in context

Status: ready-for-dev

<!-- Ultimate context engine analysis completed - comprehensive developer guide created. -->

## Story

As a **mentor**,
I want **to open a submission and read retrieved source beside the task brief**,
so that **I grade the right artifact in context**.

## Acceptance Criteria

1. **Success layout** — Given successful retrieval (**Epic 3**),
   when the mentor opens review detail,
   then **task instructions** and **source** (text or file viewer) are visible side-by-side comfortable at **~1280px**, stack on smaller widths (**FR14**, **UX-DR3**).

2. **Failure continuity** — Given retrieval failed,
   when the mentor opens detail,
   then they see **Epic 3 failure state**—not empty success UI (**FR11** continuity).

3. **Traceability** — **FR14**, **UX-DR3**.

## Tasks / Subtasks

- [ ] Detail route: load task + submission + retrieval artifact; mentor-only.
- [ ] Thymeleaf layout: two-column main / stacked mobile; reuse fragments.
- [ ] Escape source for XSS unless trust model explicitly safe (prefer text presentation).
- [ ] Tests: view renders with mock data; failed retrieval shows diagnostic partial.

## Dev Notes

### Prerequisites

- **4.1** navigation into detail; **3.x** retrieval storage.

### References

- [Source: `_bmad-output/planning-artifacts/epics.md` — Story 4.2]

## Dev Agent Record

### Agent Model Used
_(filled by dev agent)_
### Debug Log References
### Completion Notes List
### File List
_(filled by dev agent on completion)_

---

**Story completion status:** `ready-for-dev` — Ultimate context engine analysis completed.
