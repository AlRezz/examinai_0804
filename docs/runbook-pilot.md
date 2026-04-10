# Pilot operations runbook

Short guide for operators bringing up the **Examinai** pilot stack (story **7.3**), aligned with **Docker Compose** story **7.2** and ops NFRs (**NFR6**, **NFR8**, **NFR12**).

## Prerequisites

- Repo root: copy **`.env.example`** to **`.env`** and set secrets there only (never commit **`.env`**).
- **Docker Compose v2** (`docker compose`). See **`.env.example`** for port overrides, **`OLLAMA_IMAGE`**, **`OLLAMA_MODEL`**, JDBC, and Git keys.
- With the bundled **`docker-compose.yml`**, the **`llm`** service starts **`ollama serve`**, then runs **`ollama pull "$OLLAMA_MODEL"`** (default **`deepseek-r1:8b`**). The first pull can take several minutes; the **`llm`** healthcheck waits until that model appears in **`ollama list`** before **`app`** starts. To refresh or add a model manually: `docker compose exec llm ollama pull <model>`.

## Health checks

| Target | Check | Notes |
|--------|--------|--------|
| Spring Boot app | `GET /actuator/health` | Default base path `/actuator` (see `application.yml`). Compose healthcheck uses this on port **8080** inside the app container. |
| PostgreSQL | Compose `db` service `healthcheck` | `pg_isready`; app waits on `depends_on: condition: service_healthy`. |
| Ollama | Host **11434** (default); Compose **`llm`** health | Daemon up and **`OLLAMA_MODEL`** present per **`ollama list`**; inference uses **`OLLAMA_BASE_URL`**. |

Example from the host (default app port):

```bash
curl -sSf http://localhost:8080/actuator/health
```

## Git integration: HTTP status → safe diagnostics

The app maps provider HTTP results to internal **`GitFailureKind`** values and **user-safe** copy (no raw bodies or tokens in the UI). When troubleshooting, use **category + HTTP pattern** in tickets—**not** tokens or full error payloads (**NFR12**).

| Typical upstream signal | `GitFailureKind` | Operator hint (no secrets in logs) |
|-------------------------|------------------|-----------------------------------|
| `GIT_PROVIDER_BASE_URL` missing / empty | `CONFIG_MISSING` | Set base URL + token in env; redeploy. |
| HTTP **403** | `ACCESS_DENIED` | Token scope / repo visibility / org SSO. |
| HTTP **404** | `NOT_FOUND` | Wrong owner/repo, ref, or path scope. |
| HTTP **429** | `RATE_LIMIT` | Back off; retried in client. |
| HTTP **5xx** | `UPSTREAM_ERROR` | Provider outage; retried when retriable. |
| Timeout / transport failure | `TIMEOUT` | Network, firewall, or host downtime. |
| Other 4xx (e.g. **401**), parse issues | `INVALID_RESPONSE` | Treat as misconfiguration or unexpected API shape; check base URL and token type. |

Logs use redaction helpers so provider bodies and secrets are not printed verbatim.

## Degraded LLM (**NFR8**)

When the LLM service is down or unreachable (e.g. stop the **`llm`** container):

- Mentor submission pages show **degraded inference** messaging (story **5.4**).
- **Publish official review** remains available; AI draft generation is **assistive** only—mentors can proceed without a successful model call.

Verify degraded mode without sharing credentials: stop **`llm`**, open a mentor submission detail, confirm banner / CTA behavior, then `docker compose start llm` (or `up`) and re-check optional draft flow.

## Troubleshooting

### `FATAL: role "root" does not exist` (PostgreSQL)

Postgres does **not** create a `root` superuser. This usually means the **client username** does not match a role in the cluster—often after **empty or mismatched env** vs how the data volume was first initialized.

1. Ensure **`POSTGRES_USER`** matches **`SPRING_DATASOURCE_USERNAME`** (Compose passes JDBC from the same `POSTGRES_*` defaults when `SPRING_DATASOURCE_*` are unset; see **`.env.example`**).
2. If you changed **`POSTGRES_USER`** after the DB volume was created, the old role may still be in the volume. **Remove the named volume** (destructive) and bring the stack up again, or connect with the **original** role name. Example: `docker compose down -v` (drops local pilot data) then `docker compose up --build`.
3. When using **`psql`** or **`pg_isready`**, pass **`-U "$POSTGRES_USER"`** (or `-U examinai`)—these tools default the DB user to the **current OS user** (often `root` inside containers), which is not a Postgres role unless you created it.
4. The bundled **`db`** healthcheck uses **`pg_isready -U "$POSTGRES_USER"`** so it does not probe as `root`.

## Smoke path: login → retrieval → optional AI

1. **Stack up:** `docker compose up --build` (or your orchestration equivalent).
2. **Health:** `GET /actuator/health` returns **UP**.
3. **Login:** `GET /login` → sign in (see **README** for bootstrap dev admin—rotate before shared environments).
4. **Retrieval:** As a mentor, open **`/tasks/{taskId}/submissions/{internId}`** for a submission that has version-control coordinates; use the UI control that triggers **fetch** (posts to **`…/fetch`**). Confirm source text or the safe Git error panel if coordinates are wrong.
5. **Optional AI:** With **`llm`** healthy and model pulled, use **generate AI draft** on the same page; confirm draft or degraded messaging if inference fails.

## Production-oriented profile (**NFR6**, Actuator)

- **`application-prod.yml`** sets **`server.error.include-stacktrace: never`** so default error responses do not expose stack traces to browsers.
- **Actuator** web exposure in **`prod`** is limited to **`health`** only (broader exposure in **`dev`** is **`health`, `info`**). Rationale: architecture **Observability** in [`docs/planning-artifacts/architecture.md`](planning-artifacts/architecture.md) (restrict full actuator in production; use network boundaries or separate management access in real deployments).

## CI / release checklist (snippet)

- [ ] **`.env`** / secrets store populated from **`.env.example`** keys (no real values in repo).
- [ ] **`./mvnw verify`** green on the release revision.
- [ ] Compose smoke: **`docker compose up --build`**, then **`/actuator/health`** and login smoke (or equivalent in target environment).
- [ ] Optional: stop **`llm`**, confirm **NFR8** degraded UI; restart **`llm`**.

## Traceability

- **NFR8:** Health and documented degraded LLM behavior in pilot.
- **NFR12:** Env keys documented without values; Git/LLM diagnostics remain secret-safe.
- **7.4:** Postgres role / JDBC alignment and troubleshooting for `FATAL: role "root" does not exist`.
