# Story 2.4: Attach submission with version-control coordinates

Status: done

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

- [ ] Liquibase: `submissions` (or equivalent) with FK to task + intern user, fields for repo/ref/scope, status enum/text, timestamps (AC: #1).
- [ ] Service validates assignment ownership—intern can only submit for **their** task assignments (AC: #1).
- [ ] Thymeleaf: form on intern task detail or dedicated flow; CSRF on POST (AC: #1).
- [ ] **Do not** call Git APIs here—that is **Epic 3**; store coordinates only (AC: #1).
- [ ] Tests: happy path + reject not-assigned task (AC: #1).

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
_(filled by dev agent)_
### Debug Log References
### Completion Notes List
### File List
_(filled by dev agent on completion)_

---

**Story completion status:** `done` — Ultimate context engine analysis completed.