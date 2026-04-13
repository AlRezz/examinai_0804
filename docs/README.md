# Examinai documentation

This folder is the **project knowledge** root for BMad (`project_knowledge` in `docs/config.yaml`). Use it for onboarding, operations, and traceability from requirements to shipped behavior.

## What lives where

| Area | Location | Notes |
|------|----------|--------|
| Product requirements | [`planning-artifacts/prd.md`](planning-artifacts/prd.md) | PRD — scope, journeys, functional requirements |
| Architecture | [`planning-artifacts/architecture.md`](planning-artifacts/architecture.md) | Technical decisions aligned with the PRD |
| Epics and stories | [`planning-artifacts/epics.md`](planning-artifacts/epics.md) | Full FR/NFR inventory and story breakdown |
| Implementation readiness | [`planning-artifacts/implementation-readiness-report-2026-04-08.md`](planning-artifacts/implementation-readiness-report-2026-04-08.md) | Pre-build alignment check |
| Sprint change history | [`planning-artifacts/sprint-change-proposal-2026-04-08.md`](planning-artifacts/sprint-change-proposal-2026-04-08.md) | Example mid-Sprint correction record (Epic 5) |
| Sprint board | [`implementation-artifacts/sprint-status.yaml`](implementation-artifacts/sprint-status.yaml) | Authoritative `development_status`; keep in sync with `_bmad-output/implementation-artifacts/sprint-status.yaml` |
| Story write-ups | [`implementation-artifacts/`](implementation-artifacts/) | One file per story; mirrors `_bmad-output/implementation-artifacts/` |
| Operator runbook | [`runbook-pilot.md`](runbook-pilot.md) | Compose, health, Git/LLM triage, smoke path |
| AI / codebase context | [`project-context.md`](project-context.md) | Lean rules for assistants; mirrors `_bmad-output/project-context.md` |
| Brainstorming | [`brainstorming/`](brainstorming/) | Early ideation input to the PRD |

## BMad output folder

[`_bmad-output/`](../_bmad-output/) is the default BMad **output** directory (`output_folder` in BMad config). Planning and implementation artifacts are duplicated here and under `docs/` so agents and humans can follow one convention: **treat `docs/` and `_bmad-output/` as pairs** for planning artifacts, `project-context.md`, `sprint-status.yaml`, and per-story markdown—unless you standardize on a single edit path and sync.

## Root README

The repository [`README.md`](../README.md) covers build, configuration, Docker Compose, AI drafts, and role-based flows. Start there for developers; use this folder for product and Sprint traceability.
