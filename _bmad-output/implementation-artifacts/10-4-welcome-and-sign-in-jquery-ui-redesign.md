# Story 10.4: Welcome + sign-in redesign (jQuery UI widgets)

Status: done

## Tasks / Subtasks

- [x] Add **jQuery** and **jQuery UI** WebJars; permit **`/js/**`** in security config for static init script
- [x] Introduce **`head-welcome-jqui :: welcomeStyles`** (Bootstrap + `examai-theme` + jQuery UI CSS + `welcome-jqui.css`) and **`welcome-scripts`** fragments
- [x] Redesign **`index.html`** (welcome) and **`login.html`** with **`ui-widget` / `ui-widget-header` / `ui-corner-all`**, **`ui-state-highlight`** / **`ui-state-error`** banners, and **`.button()`** on primary actions
- [x] Add **`welcome-jqui.css`** (bluish header/panel chrome) and **`welcome-jqui-init.js`**; add **`body.welcome-jqui`** override in **`examai-theme.css`** so the global boxed `<main>` rule does not fight the centered card
- [x] Add **CSRF hidden field** to the login form (POST `/login`)

## Dev Agent Record

### File List

- `pom.xml`
- `src/main/java/com/examinai/app/config/SecurityConfig.java`
- `src/main/resources/static/css/examai-theme.css`
- `src/main/resources/static/css/welcome-jqui.css`
- `src/main/resources/static/js/welcome-jqui-init.js`
- `src/main/resources/templates/fragments/head-welcome-jqui.html`
- `src/main/resources/templates/fragments/welcome-scripts.html`
- `src/main/resources/templates/index.html`
- `src/main/resources/templates/login.html`

### Change Log

- 2026-04-13: Epic 10.4 — jQuery UI WebJars, centered widget cards on `/` and `/login`, button widgets, CSRF on sign-in.

## Story

As a **visitor or returning user**,
I want **the welcome and sign-in pages to use real jQuery UI styling (widgets, states, bluish chrome)**,
So that **first impressions match the product theme and the flow no longer looks like unstyled HTML**.

## Acceptance Criteria

1. **`/`** and **`/login`** load jQuery UI **CSS and JS** from WebJars (no external CDN required for pilot).
2. Both pages use **jQuery UI structure** (e.g. `ui-widget`, `ui-widget-header`, `ui-widget-content`, corners) and **primary actions** are initialized with **`.button()`**.
3. Sign-in status messages use **`ui-state-highlight`** / **`ui-state-error`** as appropriate.
4. The login **POST** includes a valid **CSRF** token.
5. Visual treatment stays **bluish** and consistent with Epic 10’s palette.
