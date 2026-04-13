# Story 9.5: Ollama — missing model pull at startup and operator troubleshooting

Status: done

## Tasks / Subtasks

- [x] Set `spring.ai.ollama.init.pull-model-strategy` to **`when_missing`** (with **`max-retries: 2`**) in default `application.yml` so the chat model is pulled when absent
- [x] Keep **`pull-model-strategy: never`** in **`application-test.yml`** so tests do not download models
- [x] Document **HTTP 404 / `model '…' not found`** in **README** (manual `ollama pull`, **`OLLAMA_MODEL`**, **`SPRING_AI_OLLAMA_INIT_PULL_MODEL_STRATEGY=never`**)
- [x] Align **pilot runbook** with Ollama troubleshooting; refresh Git fetch hint row for **`files[]` patch-only** behavior (Story 9.4)
- [x] Optional: **`.env.example`** key for disabling auto-pull

## Dev Agent Record

### Completion Notes

- Default behavior: JVM startup may trigger a one-time **Ollama pull** for **`OLLAMA_MODEL`** when the model is not local (first run can be slow).
- Production can set **`SPRING_AI_OLLAMA_INIT_PULL_MODEL_STRATEGY=never`** if models are pre-installed in the image.

### File List

- `src/main/resources/application.yml`
- `src/main/resources/application-test.yml`
- `README.md`
- `docs/runbook-pilot.md`
- `.env.example`

### Change Log

- 2026-04-13: Epic 9.5 — Ollama init pull when missing, test isolation, README/runbook/env docs.

## Story

As an **operator or developer running Examinai with Ollama**,
I want **the app to pull the configured chat model when it is missing and clear docs when inference returns 404**,
So that **mentor AI draft does not fail with an opaque “model not found” error** and **CI/tests stay offline-safe**.

## Context

Ollama returns **404** with a JSON body like `model 'deepseek-r1:8b' not found` when that tag has not been pulled into the daemon serving **`OLLAMA_BASE_URL`**. Spring AI previously used **`pull-model-strategy: never`**, so the application never repaired a fresh Ollama data directory automatically.

## Acceptance Criteria

1. **Default config** — `application.yml` sets **`spring.ai.ollama.init.pull-model-strategy`** to **`when_missing`** and **`max-retries`** to **`2`** for init pull resilience.
2. **Tests** — `application-test.yml` sets **`pull-model-strategy: never`** so the test suite does not pull models against the dummy base URL.
3. **README** — *AI draft assessments* section explains **404 / model not found**: manual **`ollama pull`**, **`OLLAMA_MODEL`**, Compose **`docker compose exec llm ollama pull`**, auto-pull on startup, and **`SPRING_AI_OLLAMA_INIT_PULL_MODEL_STRATEGY=never`** for opt-out.
4. **Runbook** — `docs/runbook-pilot.md` includes a short **Ollama 404** troubleshooting subsection consistent with README; Git diagnostic row reflects **patch-only** from **`files[]`** when matched (Story 9.4).
5. **`.env.example`** — documents optional **`SPRING_AI_OLLAMA_INIT_PULL_MODEL_STRATEGY`** (commented) near **`OLLAMA_MODEL`**.

## File List (expected touchpoints)

- `src/main/resources/application.yml`
- `src/main/resources/application-test.yml`
- `README.md`
- `docs/runbook-pilot.md`
- `.env.example`
