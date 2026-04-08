# Story 7.1: Coordinator read-only case record

Status: done

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

- [x] Role: `COORDINATOR` (or architecture-aligned name); seed/migration in **1.3** lineage if not present.
- [x] Read-only controller + service: aggregate queries across submission, retrieval, **5.2** draft/invocation, **4.4** published review.
- [x] Thymeleaf read-only template; no POST mutating review.
- [x] Tests: coordinator 200; mentor/intern/coordinator-denied cases.

## Dev Notes

### Prerequisites

- **Epics 3–5** data model; **4.4** publish provenance.

### Architecture compliance

- Coordinator path in FR mapping [Source: architecture **Requirements to Structure Mapping**].

### References

- [Source: `_bmad-output/planning-artifacts/epics.md` — Story 7.1]

## Dev Agent Record

### Agent Model Used

Cursor agent (implementation session)

### Debug Log References

- `./mvnw test` (all tests pass)

### Completion Notes List

- Added `COORDINATOR`-only `/coordinator/**` security rule; login success redirects to `/coordinator`.
- `CoordinatorCaseRecordService` aggregates submission, retrieval (via existing git fragment + message), latest AI draft + invocation metadata, mentor draft (unpublished), and published history with provenance (mentor email, `publishedAt`, snapshot).
- Read-only templates `coordinator/index.html` and `coordinator/case-record.html` (no review POST endpoints).
- Integration tests: coordinator 200 + 404 for unknown id; mentor and intern receive 403; login redirect test for coordinator.

### File List

- `src/main/java/com/examinai/app/service/CoordinatorCaseRecordModel.java`
- `src/main/java/com/examinai/app/service/CoordinatorCaseRecordService.java`
- `src/main/java/com/examinai/app/web/coordinator/CoordinatorCaseRecordController.java`
- `src/main/java/com/examinai/app/config/SecurityConfig.java`
- `src/main/java/com/examinai/app/security/RoleBasedAuthenticationSuccessHandler.java`
- `src/main/resources/templates/coordinator/index.html`
- `src/main/resources/templates/coordinator/case-record.html`
- `src/main/resources/templates/home.html`
- `src/test/java/com/examinai/app/web/CoordinatorCaseRecordIntegrationTest.java`
- `src/test/java/com/examinai/app/web/LoginAndSecurityIntegrationTest.java`
- `_bmad-output/implementation-artifacts/sprint-status.yaml`

---

**Story completion status:** `done` — Accepted and closed.
