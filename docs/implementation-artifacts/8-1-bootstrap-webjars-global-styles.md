# Story 8.1: Bootstrap WebJars — global styles on all pages

Status: done

## Story

As a **user**,
I want **consistent Bootstrap styling across every Thymeleaf screen**,
so that **the app looks coherent and works without relying on external CDNs** (e.g. pilot / offline / Compose).

## Acceptance Criteria

1. **Bootstrap 5** CSS is bundled via **`org.webjars:bootstrap`** and linked from a shared Thymeleaf fragment (`fragments/head-bootstrap`).
2. **All** full-page templates include the fragment (login, home, tasks, admin, mentor, intern, coordinator — not only a subset).
3. **Spring Security** allows unauthenticated access to **`/webjars/**`** so styles load on `/login` and other public routes.
4. **No jsDelivr dependency** for Bootstrap in templates (removed CDN `<link>` tags).

## Completion notes

- **Dependency:** `org.webjars:bootstrap:5.3.3` in `pom.xml`.
- **Security:** `SecurityConfig` — `requestMatchers("/webjars/**").permitAll()`.
- **Templates:** `templates/fragments/head-bootstrap.html` fragment; every page `<head>` includes `th:replace="~{fragments/head-bootstrap :: styles}"`.

## File List

- `pom.xml`
- `src/main/java/com/examinai/app/config/SecurityConfig.java`
- `src/main/resources/templates/fragments/head-bootstrap.html`
- `src/main/resources/templates/**/*.html` (full pages updated)

## Change Log

| Date       | Change |
| ---------- | ------ |
| 2026-04-10 | Implemented WebJars Bootstrap; sprint / docs marked done |
