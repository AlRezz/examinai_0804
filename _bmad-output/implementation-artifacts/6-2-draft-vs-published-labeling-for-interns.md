# Story 6.2: Draft vs published labeling for interns

Status: review

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

- [x] Integrate **5.2** draft rows into intern view when policy flag allows; CSS/labels: “Draft — not official”.
- [x] Product policy: document when interns see drafts (config or hardcoded MVP rule).
- [x] Tests: with draft + publish, labels correct; publish-only, no misleading AI block.

## Dev Notes

### Prerequisites

- **5.2** draft persistence; **6.1** feedback page shell.

### Architecture compliance

- Draft vs published separation in persistence unchanged [Source: architecture **Data boundaries**].

### References

- [Source: `_bmad-output/planning-artifacts/epics.md` — Story 6.2]

## Dev Agent Record

### Agent Model Used

Composer (Cursor agent)

### Debug Log References

_(none)_

### Completion Notes List

- `examinai.intern.show-ai-draft-to-intern` (default `true`) gates `AiDraftPersistenceService` output on the intern feedback page; when `false`, the AI panel is omitted entirely.
- Feedback template uses a dashed warning panel, badge **Draft — not official**, and copy stating the model output is not a grade; official mentor block is separate and listed first.

### File List

- `src/main/java/com/examinai/app/config/InternUiProperties.java`
- `src/main/java/com/examinai/app/config/InternUiConfiguration.java`
- `src/main/resources/application.yml`
- `src/main/java/com/examinai/app/service/InternFeedbackService.java`
- `src/main/resources/templates/intern/submissions/feedback.html`
- `src/test/java/com/examinai/app/config/InternUiPropertiesBindingTest.java`
- `src/test/java/com/examinai/app/web/Epic6InternSurfacesIntegrationTest.java` (`internSeesLabeledAiDraftSeparateFromOfficialWhenBothExist`, plus publish-only paths in other cases)

## Change Log

- 2026-04-08: Intern-visible AI draft policy + labeled non-official panel on feedback page.

---

**Story completion status:** `review` — Implementation complete; ready for code review.
