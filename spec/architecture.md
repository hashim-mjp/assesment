# Architecture — Support Ticket Management System

Derived from `spec/requirements.md` and aligned with `rules/java-springboot.md` and `rules/api-standards.md`. This document describes system shape only; it does **not** define endpoint payloads (`api-contract.md`), transition tables (`state-machine.md`), or screen wireframes (`ui-flow.md`).

---

## High-level shape

```
┌─────────────────────────────────────┐
│  Frontend (React / Next.js)         │
│  Pages → API client → fetch/HTTP    │
└─────────────────┬───────────────────┘
                  │ REST / JSON
                  │ /api/...
┌─────────────────▼───────────────────┐
│  Backend (Java 21 + Spring Boot)    │
│  api → application → domain         │
│                 ↘ persistence       │
└─────────────────┬───────────────────┘
                  │ JPA / JDBC
┌─────────────────▼───────────────────┐
│  Database                           │
│  PostgreSQL (target)                │
│  H2 file-based (optional local/dev) │
└─────────────────────────────────────┘
```

Monorepo or sibling apps are both acceptable; keep **backend** and **frontend** as separate runnable units sharing only the HTTP contract.

---

## Backend modules

Package layout follows `rules/java-springboot.md`. Dependency direction: **Controller → Service → Domain / Repository** (never reverse).

| Module / package | Responsibility |
|------------------|----------------|
| **`…/api`** | REST controllers, request/response DTOs (records), `@Valid` at the boundary, `@RestControllerAdvice` for HTTP error mapping |
| **`…/application`** | Use-case services, orchestration, `@Transactional` write paths |
| **`…/domain`** | Ticket/Comment aggregates (or entities), enums (`TicketStatus`, priority if enum), **status transition rules** |
| **`…/persistence`** | Spring Data JPA repositories, JPA mappings if split from domain, query methods for list/search/filter |
| **`…/config`** | Non-secret beans (CORS for local, datasource wiring via env, profiles) |

### Layer rules (summary)

- Controllers map HTTP ↔ DTOs and status codes; **no** business rules or direct repository calls.
- Services orchestrate create/list/update/comment/search/filter and enforce domain invariants via domain code.
- Repositories persist and load; they do **not** own status-machine policy.
- DTOs stay free of JPA annotations.

Exact Maven/Gradle multi-module vs single-module packaging is an implementation choice; layer boundaries above are mandatory.

---

## Frontend app shape

Stack: **React / Next.js** (App Router or Pages Router). The UI is a thin client over the REST API.

| Route (suggested) | Purpose | Maps to requirement |
|-------------------|---------|---------------------|
| `/` or `/tickets` | Ticket **list** with keyword search and status filter controls | FR2, FR6, FR7 |
| `/tickets/new` | **Create** ticket form | FR1 |
| `/tickets/[id]` | Ticket **detail**: fields, status actions, comments list + add-comment | FR3, FR4, FR5 |

Optional convenience: search/filter may live only on the list route via query params (e.g. `?q=` / `?status=`) rather than a separate page — same capability, fewer routes.

### UI responsibilities

- Call REST endpoints; render success and error states.
- Show **meaningful errors** from the API error body (`message`, optional `details`) per `rules/api-standards.md` (FR10).
- May hide illegal status actions for UX; **must not** be the only enforcement of the state machine (backend still rejects).

Detailed navigation and form behaviour: `spec/ui-flow.md` (later).

---

## How the UI talks to the REST API

1. Browser loads Next.js pages.
2. Client-side (or Next.js server components / route handlers acting as BFF) issues HTTP requests to the Spring Boot base URL, e.g. `http://localhost:8080/api/...`.
3. Content-Type: `application/json`.
4. List/search/filter use `GET` with query params (`q`/`search`, `status`, pagination per API standards).
5. Create/update/comment use `POST` / `PUT`/`PATCH` as defined in `spec/api-contract.md`.
6. On 4xx/5xx, parse the **stable error shape** (`code`, `message`, `details`) and display it; do not invent silent no-ops.

**CORS:** allow the frontend origin in a **local/dev** profile only as needed; do not ship permissive CORS as a production default (`rules/java-springboot.md`).

**Auth:** not required by `requirements.md` — see Open questions.

Base URL and path prefix (`/api` vs `/api/v1`) are fixed in `api-contract.md`.

---

## Database choice

| Environment | Store | Notes |
|-------------|-------|--------|
| **Production-like / acceptance durability** | **PostgreSQL** | Realistic persistence; default target for demos that must prove restart survival against a real RDBMS |
| **Local / fast dev** | **H2** allowed | Prefer **file-based** H2 (not in-memory) if used to satisfy “data survives restart” without Postgres |
| **Automated tests** | H2 in-memory **or** Testcontainers PostgreSQL | Per `rules/java-springboot.md`; test profile must not rely on committed secrets |

### Path from H2 → PostgreSQL

1. Use Spring profiles: e.g. `local` (H2 file) and `postgres` / default (PostgreSQL).
2. Keep JPA entities and repositories **database-agnostic** (standard SQL types; avoid H2-only SQL in production code).
3. Configure datasource only via env / external config:
   - `SPRING_DATASOURCE_URL`
   - `SPRING_DATASOURCE_USERNAME`
   - `SPRING_DATASOURCE_PASSWORD`
4. Commit `.env.example` or sample `application-*.yml` with **placeholders only** (no real passwords).
5. Schema: Flyway/Liquibase **or** documented `ddl-auto` strategy — choose one in implementation and keep migrations portable across both DBs where practical.

---

## How data survives application restart

- Ticket and comment rows live in the **database**, not in process memory or ephemeral caches (FR8).
- With **PostgreSQL**: data remains after JVM stop/start as long as the DB volume/service persists.
- With **H2 local/dev**: use **file mode** (e.g. `jdbc:h2:file:./data/tickets`) and document the file path; **in-memory H2 does not** satisfy acceptance for restart survival.
- Application restart alone must not wipe durable data when the durable profile is active (Core Acceptance Criteria).

Schema evolution and backup are out of scope unless added later.

---

## Validation and error mapping

| Concern | Where it lives |
|---------|----------------|
| Request shape (required fields, sizes, enum parse) | API layer: Bean Validation on DTOs (`@Valid`, `@NotBlank`, …) |
| Domain rules (status transitions, invariants) | Domain / application services — **never** UI-only |
| HTTP status + error JSON | `@RestControllerAdvice` in `…/api`, mapping domain/application exceptions to the shared body in `rules/api-standards.md` |
| Unexpected failures | Mapped to generic `500` client message; log without secrets/PII dumps |

Invalid input → reject with **4xx** and stable `code` / `message` (FR9, FR10). Exact codes for illegal transitions are specified in `state-machine.md` / `api-contract.md`.

---

## Secrets stay out of git

| Do | Do not |
|----|--------|
| Env vars / IDE run configs / secret managers for DB passwords and any future API keys | Hardcode credentials in source, tests, or docs |
| Commit `.env.example`, sample YAML with placeholders | Commit `.env`, real `application-local.yml` with passwords |
| Ignore secret files via `.gitignore` | Log connection passwords or tokens |
| Rotate if a secret is ever committed | Paste secrets into prompt history or specs |

No intentional auth/validation bypasses in shared defaults (NFR security defaults).

---

## Traceability

| Requirement | Architecture coverage |
|-------------|----------------------|
| FR1–FR5, FR8 | Backend modules + persistence + frontend routes |
| FR6–FR7 | List page + API query params + DB indexes (see `data-model.md`) |
| FR9 | Bean Validation + domain rules + advice |
| FR10 | Error body → UI display |
| Restart survival | Durable DB configuration |
| No secrets in git | Config / `.gitignore` practice above |

---

## Open questions

Deferred product/tech choices — **do not invent** until decided in later specs or explicitly by the assignee:

1. **Authentication / multi-user identity** — requirements do not require login; assignee/author may be plain strings (see `data-model.md`).
2. **Frontend hosting** — same-origin reverse proxy vs separate origins (affects CORS only).
3. **API versioning** — `/api` vs `/api/v1` (lock in `api-contract.md`).
4. **Schema migration tool** — Flyway/Liquibase vs profile-specific `ddl-auto`.
5. **Whether list responses are paginated** — preferred by API standards; simplify only if documented in the contract.
6. **Exact status transition HTTP code** (`400` vs `409` vs `422`) — lock in `state-machine.md` / `api-contract.md`.
7. **Deployment topology** — single host vs container compose for Postgres; not required by requirements beyond durability and secrets hygiene.
