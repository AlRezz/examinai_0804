# Story 10.3: Application-wide bluish UI shell (jQuery UI–inspired palette)

Status: done

## Tasks / Subtasks

- [x] Add global stylesheet `static/css/examai-theme.css` (Bootstrap variable overrides + jQuery UI Smoothness-style blues; no `jquery-ui.css` to avoid clashes)
- [x] Load theme from `fragments/head-bootstrap.html` after Bootstrap so all pages that use the fragment pick it up
- [x] Tune plain `<main>` pages (login, home) and Bootstrap cards/forms/alerts for cohesive blue chrome

## Dev Agent Record

### File List

- `src/main/resources/static/css/examai-theme.css`
- `src/main/resources/templates/fragments/head-bootstrap.html`

### Change Log

- 2026-04-13: Epic 10.3 — global bluish theme aligned with classic jQuery UI blues over Bootstrap 5.

## Story

As a **user of the product**,
I want **a consistent, calm bluish visual shell across every screen**,
So that **the app feels cohesive and familiar (similar to classic jQuery UI blues) without changing behavior**.

## Acceptance Criteria

1. Every template that includes `fragments/head-bootstrap :: styles` loads **`/css/examai-theme.css`** after Bootstrap.
2. Primary actions, links, page background, and card chrome use a **jQuery UI Smoothness–style blue** palette (header accent `#5c9ccc`, primary `#2b6cb0`, light blue-gray page tint).
3. Login and minimal home-style pages with bare `<main>` receive **boxed, widget-like** treatment readable on the tinted background.
4. Existing Bootstrap components (cards, buttons, alerts, forms) remain usable; theme adjusts variables and light borders/shadows only.
