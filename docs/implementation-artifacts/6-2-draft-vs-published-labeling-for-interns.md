# Story 6.2: Draft vs published labeling for interns

Status: done

<!-- Ultimate context engine analysis completed - comprehensive developer guide created. -->

## Story

As an **intern**,
I want **AI-assisted material clearly marked as draft when policy shows it at all**,
so that **I do not confuse model text with my official grade**.

## Acceptance Criteria

1. **Visual distinction** — Given policy allows intern visibility of AI draft (before or after publish, per product rules),
   when I view the page,
   then draft sections are **visually and textually** distinct from **Official mentor feedback** (**FR23**, **UX-DR5**).

2. **Mentor primary** — Given only published mentor outcome exists,
   when I view the page,
   then mentor block is **primary**; no pattern that reads like an “AI final grade” (**FR23**).

3. **Traceability** — **FR23**, **UX-DR5**, **UX-DR6** alignment with **6.1** layout.

## Tasks / Subtasks

- [ ] Integrate **5.2** draft rows into intern view when policy flag allows; CSS/labels: “Draft — not official”.
- [ ] Product policy: document when interns see drafts (config or hardcoded MVP rule).
- [ ] Tests: with draft + publish, labels correct; publish-only, no misleading AI block.

## Dev Notes

### Prerequisites

- **5.2** draft persistence; **6.1** feedback page shell.

### Architecture compliance

- Draft vs published separation in persistence unchanged [Source: architecture **Data boundaries**].

### References

- [Source: `_bmad-output/planning-artifacts/epics.md` — Story 6.2]

## Dev Agent Record

### Agent Model Used
_(filled by dev agent)_
### Debug Log References
### Completion Notes List
### File List
_(filled by dev agent on completion)_

---

**Story completion status:** `done` — Ultimate context engine analysis completed.
