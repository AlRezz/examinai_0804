# Pilot operations runbook

Short guide for operators bringing up the **Examinai** pilot stack (story **7.3**). It aligns with the **Docker Compose** topology from story **7.2** and with operations NFRs (**NFR6**, **NFR8**, **NFR12**): health exposure, degraded LLM behavior, and secret-safe diagnostics.

## Prerequisites

- Repo root: copy **`.env.example`** to **`.env`** and set secrets there only (never commit **`.env`**).
- **UI (story 8.1):** Bootstrap **5** is served from **WebJars** (`/webjars/bootstrap/...`); no CDN required for styles in pilot or offline use. Spring Security permits **`/webjars/**`** for public routes (e.g. `/login`).
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
| Mentor UI **not found** on fetch (`NOT_FOUND`) | — | App calls **Get a commit**; a matching **`files[]`** row supplies **`patch`** only; otherwise **repository contents** for **path scope** at **ref**. **Path scope** is required. See [REST commits](https://docs.github.com/en/rest/commits/commits?apiVersion=2026-03-10), [contents](https://docs.github.com/en/rest/repos/contents?apiVersion=2026-03-10), **README**. |
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

### Ollama HTTP **404** — `model '…' not found` (inference)

The LLM runtime only serves models present in **`ollama list`** for the daemon at **`OLLAMA_BASE_URL`**.

1. **Pull the tag** configured as **`OLLAMA_MODEL`** (default **`deepseek-r1:8b`**): on the host, `ollama pull <tag>`; under Compose, `docker compose exec llm ollama pull "$OLLAMA_MODEL"` (or the concrete tag).
2. Or set **`OLLAMA_MODEL`** to a tag you already have.
3. The app uses **`spring.ai.ollama.init.pull-model-strategy: when_missing`** by default so a **missing** chat model may be **pulled at startup** (first pull can take several minutes). To **disable** auto-pull (e.g. golden images), set **`SPRING_AI_OLLAMA_INIT_PULL_MODEL_STRATEGY=never`** — see **README** → *AI draft assessments*.

## Troubleshooting

### `FATAL: role "root" does not exist` (PostgreSQL)

Postgres does **not** create a `root` superuser. This usually means the **client username** does not match a role in the cluster—often after **empty or mismatched env** vs how the data volume was first initialized.

1. Ensure **`POSTGRES_USER`** matches **`SPRING_DATASOURCE_USERNAME`** (Compose passes JDBC from the same `POSTGRES_*` defaults when `SPRING_DATASOURCE_*` are unset; see **`.env.example`**).
2. If you changed **`POSTGRES_USER`** after the DB volume was created, the old role may still be in the volume. **Remove the named volume** (destructive) and bring the stack up again, or connect with the **original** role name. Example: `docker compose down -v` (drops local pilot data) then `docker compose up --build`.
3. When using **`psql`** or **`pg_isready`**, pass **`-U "$POSTGRES_USER"`** (or `-U examinai`)—these tools default the DB user to the **current OS user** (often `root` inside containers), which is not a Postgres role unless you created it.
4. The bundled **`db`** healthcheck uses **`pg_isready -U "$POSTGRES_USER"`** so it does not probe as `root`.

### `docker-credential-desktop`: executable file not found (Compose / image pull)

Docker reads **`~/.docker/config.json`**. If it lists **`"credsStore": "desktop"`** (Docker Desktop), the CLI runs **`docker-credential-desktop`** for registry access. Some terminals (including IDE-integrated ones) do not put Docker Desktop’s **`bin`** directory on **`PATH`**, so **`docker compose build`** or **`docker compose up --build`** fails with **`error getting credentials`** before images are pulled. The same happens when **`docker`** is installed via **Homebrew** (`/opt/homebrew/bin/docker`) but credential helpers live under **Docker Desktop’s** app bundle.

**Repo helper:** from the project root, run **`./scripts/docker-with-desktop-path.sh compose …`**. It prepends Docker Desktop’s **`bin`** (so **`docker-credential-desktop`** is on **`PATH`**) and calls Homebrew’s **`docker`** when installed so **`docker compose`** still finds the Compose v2 CLI plugin, e.g. **`./scripts/docker-with-desktop-path.sh compose up -d --build`**.

1. **Quick check (macOS):** prepend Docker’s bundled tools, then retry:
   ```bash
   export PATH="/Applications/Docker.app/Contents/Resources/bin:$PATH"
   docker compose build
   ```
2. **Or** run Compose from **Terminal.app** / **iTerm** after Docker Desktop is running (same PATH fix is often applied automatically).
3. **Last resort:** back up **`~/.docker/config.json`**, remove the **`"credsStore": "desktop"`** line, and try again. Anonymous pulls of public images on Docker Hub usually still work; add this only if you understand the impact on private registries / `docker login`.

### `./mvnw package` exits **134** (`Aborted (core dumped)`) during `docker compose build`

Usually the **Maven JVM hit the container memory limit** (Linux OOM killer sends **SIGABRT**, exit **128+6**). The **`Dockerfile`** caps **`MAVEN_OPTS`** heap for the build stage; if it still fails:

1. In **Docker Desktop → Settings → Resources**, raise **Memory** (for example **6 GB** or more for Spring builds) and retry **`docker compose build`**.
2. Close other heavy processes during the image build.
3. If you customize the **`Dockerfile`**, avoid raising **`MAVEN_OPTS`** beyond what the build container is allowed to use.

## Program tasks and user accounts

- **`/tasks/**`** is authorized for **mentors and administrators** (create/edit tasks, **Assign interns**, submissions). See **README** → *User flows (by role)*.
- **Creating user accounts** (e.g. intern logins) remains **`/admin/users`**, **administrator-only**—mentors assign only users that already have the **intern** role.

## Smoke path: login → retrieval → optional AI

1. **Stack up:** `docker compose up --build` (or your orchestration equivalent).
2. **Health:** `GET /actuator/health` returns **UP**.
3. **Login:** `GET /login` → sign in (see **README** for bootstrap dev admin—rotate before shared environments).
4. **Tasks (mentor or administrator):** Open **`/tasks`**, create a task if needed (**`/tasks/new`**), then **Assign interns** (**`/tasks/{taskId}/assignments`**) so an intern can submit coordinates (intern accounts must exist first—**`/admin/users`** as administrator).
5. **Retrieval:** As a mentor (or administrator), open **`/tasks/{taskId}/submissions/{internId}`** for a submission that has version-control coordinates; use the UI control that triggers **fetch** (posts to **`…/fetch`**). Confirm source text or the safe Git error panel if coordinates are wrong.
6. **Optional AI:** After **successful fetch**, with **`llm`** healthy and the model available, use **Generate AI draft** — the app calls the LLM with the **fetched** text; the assistive response should include **feedback on the code** and **suggestions to improve** (see **README** → *Mentor or administrator*). Confirm the draft appears in **read-only** fields, then exercise **Save review draft** / **Publish official review** as needed. If inference fails, confirm degraded messaging (**NFR8**).

## Production-oriented profile (**NFR6**, Actuator)

- **`application-prod.yml`** sets **`server.error.include-stacktrace: never`** so default error responses do not expose stack traces to browsers.
- **Actuator** web exposure in **`prod`** is limited to **`health`** only (broader exposure in **`dev`** is **`health`, `info`**). Rationale: architecture **Observability** in [`docs/planning-artifacts/architecture.md`](planning-artifacts/architecture.md) (restrict full actuator in production; use network boundaries or separate management access in real deployments).

## CI / release checklist (snippet)

- [ ] **`.env`** / secrets store populated from **`.env.example`** keys (no real values in repo).
- [ ] **`./mvnw verify`** green on the release revision.
- [ ] Compose smoke: **`docker compose up --build`**, then **`/actuator/health`** and login smoke (or equivalent in target environment).
- [ ] Optional: stop **`llm`**, confirm **NFR8** degraded UI; restart **`llm`**.

## Traceability

- **Epic 9:** Mentor AI draft on **fetched** text (structured feedback + suggestions), **read-only** display on submission detail, then rubric and publish — see **README** (*Mentor or administrator — move work through review and publish*, steps 5–7). Story **9.5:** Ollama **model missing** / **404** troubleshooting and init **pull when missing**.
- **NFR8:** Health and documented degraded LLM behavior in pilot.
- **NFR12:** Env keys documented without values; Git/LLM diagnostics remain secret-safe.
- **7.4:** Postgres role / JDBC alignment and troubleshooting for `FATAL: role "root" does not exist`.
- **8.1:** Bootstrap WebJars on all Thymeleaf pages; **`/webjars/**`** documented for operators verifying static asset access.
