# Story 11.1: Mentor review navigation — buttons on queue and submission review

Status: done

## Tasks / Subtasks

- [x] **`tasks/submission-detail.html`** — Replace header **Review queue** / **Submissions for task** plain links with **`a.btn btn-sm`** (outline primary) in a flex row with gap
- [x] **`review/queue.html`** — **Program tasks** / **Home** as matching **`btn-sm`** outline buttons
- [x] **`tasks/submissions.html`** — **Back to tasks** and row **Open** as button-styled anchors; align page shell with **`bg-body-secondary`** + **`container`** like other mentor pages

## Dev Agent Record

### File List

- `src/main/resources/templates/tasks/submission-detail.html`
- `src/main/resources/templates/review/queue.html`
- `src/main/resources/templates/tasks/submissions.html`

### Change Log

- 2026-04-13: Epic 11.1 — Mentor navigation links as Bootstrap buttons on submission review, review queue, and submissions-for-task list.

## Story

As a **mentor or administrator**,
I want **key navigation between the review queue, task submission list, and submission review to look and behave like buttons**,
So that **I can find and use those actions quickly on mentor-critical flows**.

## Acceptance Criteria

1. Submission review header: **Review queue** and **Submissions for task** are Bootstrap **button-styled** links with adequate spacing.
2. Review queue header: **Program tasks** and **Home** use the same affordance pattern.
3. Submissions-for-task page: **Back to tasks** and **Open** are button-styled; layout remains readable.
4. Keyboard focus: logical tab order; visible focus (Bootstrap defaults).
