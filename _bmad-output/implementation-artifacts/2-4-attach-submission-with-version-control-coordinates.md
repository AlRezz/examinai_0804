# Story 2.4: Attach submission with version-control coordinates

Status: review

<!-- Ultimate context engine analysis completed - comprehensive developer guide created. -->

## Story

As an **intern**,
I want **to attach a submission to my task by supplying repository and revision (and scope fields as defined in this story)**,
so that **mentors can review the exact code I point to**.

## Acceptance Criteria

1. **Create submission** — Given an intern and an **assigned** task (**2.2–2.3**),
   when they submit coordinates (repo identifier, commit SHA, optional path scope per MVP rules),
   then a **submission** record is created in **draft or submitted** state suitable for **Epic 3** retrieval (**FR9**).

2. **Coordinate correction** — Given policy allows revision,
   when the intern updates coordinates,
   then support **at least** “newer submission wins” or a documented revision strategy—full **FR28** history can be deferred if noted in Dev Agent Record (**FR28** prep).

3. **SLA input hygiene** — Collect enough metadata for later **NFR3** Git fetch (timeouts bounded in **Epic 3**).

4. **Traceability** — **FR9**, **NFR3** (inputs for later SLA).

## Tasks / Subtasks

- [x] Liquibase: `submissions` (or equivalent) with FK to task + intern user, fields for repo/ref/scope, status enum/text, timestamps (AC: #1).
- [x] Service validates assignment ownership—intern can only submit for **their** task assignments (AC: #1).
- [x] Thymeleaf: form on intern task detail or dedicated flow; CSRF on POST (AC: #1).
- [x] **Do not** call Git APIs here—that is **Epic 3**; store coordinates only (AC: #1).
- [x] Tests: happy path + reject not-assigned task (AC: #1).

## Dev Notes

### Prerequisites

- **2.3** intern can see assigned tasks.

### Architecture compliance

- Future Git access only via **`integration.git`** [Source: architecture]; **no** ad-hoc HTTP from this controller.

### Data minimization

- Store coordinates + metadata; avoid large blobs (**architecture** data architecture).

### References

- [Source: `_bmad-output/planning-artifacts/epics.md` — Story 2.4]
- [Source: `_bmad-output/planning-artifacts/architecture.md` — Data Architecture, integration boundaries]

## Dev Agent Record

### Agent Model Used

Composer (Cursor agent)

### Debug Log References

### Completion Notes List

- Added `submissions` with unique `(task_id, intern_user_id)`. `SubmissionService.upsertCoordinates` updates the existing row when present — **newest wins** (MVP; no history table). Status stored as enum string (`SUBMITTED` on save; `DRAFT` reserved). POST `/intern/tasks/{taskId}/submission` with validated `SubmissionForm`. Rejects upsert when intern is not assigned. No Git client calls.

### FR28 deferral

- Full submission revision history out of scope; single upserted row per intern+task documents MVP policy.

### File List

- `src/main/resources/db/changelog/changes/003-epic2-tasks-assignments-submissions.yaml` (submissions section)
- `src/main/java/com/examinai/app/domain/task/SubmissionStatus.java`
- `src/main/java/com/examinai/app/domain/task/Submission.java`
- `src/main/java/com/examinai/app/domain/task/SubmissionRepository.java`
- `src/main/java/com/examinai/app/service/SubmissionService.java`
- `src/main/java/com/examinai/app/web/intern/SubmissionForm.java`
- `src/main/java/com/examinai/app/web/intern/InternTaskController.java`
- `src/main/resources/templates/intern/tasks/detail.html`
- `src/test/java/com/examinai/app/web/Epic2TaskAndInternIntegrationTest.java`

---

**Story completion status:** `review` — Implementation complete; run independent code review per workflow.
