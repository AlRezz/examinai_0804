# Story 7.1: Coordinator read-only case record

Status: ready-for-dev

<!-- Ultimate context engine analysis completed - comprehensive developer guide created. -->

## Story

As a **coordinator**,
I want **a read-only view of submission, retrieval, AI draft, and published mentor decision for one case**,
so that **I can answer audit questions without write access**.

## Acceptance Criteria

1. **Case read-only** — Given **coordinator** (or auditor) role,
   when opening a case by **submission identifier**,
   then view shows: Git ref/coordinates, **retrieval** outcome summary, **AI invocation** metadata, **draft vs published** distinction, **publisher id and time** (**FR26**, **UX-DR9**).

2. **No writes** — Coordinators **cannot** mutate reviews or scores through this surface.

3. **Denied for others** — Without coordinator role,
   when hitting the same URL,
   then access denied (**FR4** extension).

4. **Traceability** — **FR26**, **UX-DR9**.

## Tasks / Subtasks

- [ ] Role: `COORDINATOR` (or architecture-aligned name); seed/migration in **1.3** lineage if not present.
- [ ] Read-only controller + service: aggregate queries across submission, retrieval, **5.2** draft/invocation, **4.4** published review.
- [ ] Thymeleaf read-only template; no POST mutating review.
- [ ] Tests: coordinator 200; mentor/intern/coordinator-denied cases.

## Dev Notes

### Prerequisites

- **Epics 3–5** data model; **4.4** publish provenance.

### Architecture compliance

- Coordinator path in FR mapping [Source: architecture **Requirements to Structure Mapping**].

### References

- [Source: `_bmad-output/planning-artifacts/epics.md` — Story 7.1]

## Dev Agent Record

### Agent Model Used
_(filled by dev agent)_
### Debug Log References
### Completion Notes List
### File List
_(filled by dev agent on completion)_

---

**Story completion status:** `ready-for-dev` — Ultimate context engine analysis completed.
