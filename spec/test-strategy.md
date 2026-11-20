# Test strategy — Support Ticket Management System

Derived from `spec/requirements.md` (Core Acceptance Criteria), `spec/state-machine.md`, `spec/api-contract.md`, and `rules/testing.md`. This document defines **what** we prove and **where**; `rules/testing.md` defines **how** (pyramid, naming, anti-patterns).

**No production code in this document.** Tests are written later (see `commands/generate-tests.md`). Specs win over any generated test that invents endpoints, statuses, or transitions.

---

## Goals

| Goal | Meaning |
|------|---------|
| Spec-driven | Assert behaviour from `spec/`, not implementation trivia |
| High signal | Prefer few decisive tests over snapshot / pixel noise |
| Backend truth | State machine and validation proven via API (or the service boundary the API uses) — never UI-only |
| Acceptance-mapped | Every Core Acceptance Criterion has an explicit proof type and location |
| Secrets hygiene | Fixtures, CI config, and repo checks prove no real credentials are committed |

---

## Test pyramid (this project)

Aligned with `rules/testing.md`:

| Type | Tools (typical) | Use for |
|------|-----------------|---------|
| **Unit** | JUnit 5, AssertJ; Mockito sparingly | Domain validation helpers, transition allow/deny policy in isolation |
| **Slice / API** | `@WebMvcTest`, `@DataJpaTest`, MockMvc | REST contract, status codes, error body shape, list `q` / `status` query handling |
| **Integration** | `@SpringBootTest` + MockMvc or TestRestTemplate; **H2** or **Testcontainers Postgres** | Persistence across context/DB session, comments, **full state-machine suite** |
| **Frontend** (sparse) | React Testing Library / Vitest or Jest | Form field errors from API `details`, list search/filter wiring — only where they add confidence |

Default CI / local test command must run unit + slice + integration suites required for acceptance. Do not claim green without running them.

### Suggested locations (mirror production packages)

Exact package root follows implementation; names below are the intended structure:

| Suite | Location (indicative) |
|-------|------------------------|
| Domain unit | `src/test/java/.../domain/` (e.g. `TicketStatusTransitionTest`, validation helpers) |
| API slice | `src/test/java/.../api/` (e.g. `TicketControllerWebMvcTest`) |
| Persistence slice (optional) | `src/test/java/.../persistence/` |
| Integration | `src/test/java/.../integration/` |
| State-machine integration | `src/test/java/.../integration/TicketStateMachineIT` (or equivalent dedicated class) |
| Frontend | `frontend/` (or Next app) `**/__tests__/**` or co-located `*.test.tsx` — **minimal** |

Share builders/fixtures; one logical behaviour per test; name transition tests after the edge (e.g. `allowsOpenToInProgress`, `rejectsClosedToOpen`).

---

## Unit tests — domain validation and transitions

**Purpose:** Fast, deterministic checks of pure domain rules without Spring or HTTP.

| Behaviour | Source | Proof |
|-----------|--------|-------|
| Field invariants (blank title/description, max lengths per contract) | `api-contract.md` field limits; FR9 | Unit tests on validators / domain factories |
| Create always starts as `OPEN`; client status ignored at domain level if tested here | `state-machine.md` | Unit: new ticket status = `OPEN` |
| Transition **policy** allow/deny for the 5×5 matrix | `state-machine.md` transition table | Unit: policy returns allow/deny for each cell (optional but valuable; **does not replace** integration suite) |
| Edge cases | Empty strings, oversized fields, null IDs | Unit |

Unit transition tests may use a pure policy function. They **must not** be the only proof of HTTP rejection — that belongs to the state-machine **integration** suite.

---

## API / slice tests — REST contract and error bodies

**Purpose:** Controllers + advice map domain results to the contract without a full DB (or with thin mocks). Prefer asserting status code + error JSON shape over internal method calls.

| Behaviour | Endpoint / rule | Assertions |
|-----------|-----------------|------------|
| Create validation | `POST /api/tickets` | `400` `VALIDATION_ERROR`; body has `timestamp`, `status`, `error`, `code`, `message`, `path`; optional `details[]` |
| Create rejects client `status` | Create body with `status` | `400` `VALIDATION_ERROR` |
| Update rejects `status` in patch body | `PATCH`/`PUT` per contract | `400` `VALIDATION_ERROR` |
| Not found | Get/update/transition unknown id | `404` `NOT_FOUND` |
| Unknown enum | Bad `status` / priority string | `400` `VALIDATION_ERROR` (not `INVALID_TRANSITION`) |
| Error body shape | Any 4xx | Matches `rules/api-standards.md` / `api-contract.md` |
| List query params | `GET /api/tickets?q=&status=` | Controller/service receives params; unknown `status` → `400` (slice or integration) |

Slice tests prove **contract**; persistence of search results is proven in integration (below).

---

## Integration tests — real database and restart / persistence

**Purpose:** Prove FR8 and “data survives application restart” with a real-ish store.

### Database choice for tests

| Option | When | Notes |
|--------|------|-------|
| **Testcontainers PostgreSQL** | Preferred for acceptance-like durability | Matches production-like path in `architecture.md` |
| **H2** | Acceptable for local/CI speed | Prefer a mode that still exercises JPA repositories; document profile. In-memory H2 is fine for most ITs; **restart survival** must still be demonstrated (see below) |

No datasource passwords with real values in committed test config — use Testcontainers defaults, ephemeral H2 URLs, or env placeholders.

### How we prove data survives restart / is persisted

Pick **at least one** of these patterns (document which in the IT class):

1. **New Spring context / new DB session:** Create ticket (+ optional comment) via API; flush/commit; start a **new** `@SpringBootTest` context or clear persistence context and reload by id from a fresh repository/API call — row still present with same field values.
2. **Process-style restart (stronger):** With file-based H2 or Testcontainers Postgres volume/session that outlives the JVM context: create data → stop context → start new context against same store → `GET` returns the ticket.
3. **Manual / demo checklist (supplementary):** With durable profile (Postgres or file H2), create via UI or API, restart the app process, confirm data still listed — required for human acceptance; automation covers (1) or (2).

Comments must also persist (create comment → reload ticket detail → comment present).

---

## State-machine integration suite (required)

**Class:** dedicated integration suite (e.g. `TicketStateMachineIT`).

**Authority:** Copy the matrix from `spec/state-machine.md`. Every cell → at least one test. Call **`POST /api/tickets/{id}/transition`** (or the service boundary used by that endpoint) — **not** UI clicks alone.

### Allowed transitions (must succeed)

| From | To | Expected |
|------|-----|----------|
| `OPEN` | `IN_PROGRESS` | `2xx`; persisted status = `IN_PROGRESS` |
| `OPEN` | `CANCELLED` | `2xx`; status = `CANCELLED` |
| `IN_PROGRESS` | `RESOLVED` | `2xx`; status = `RESOLVED` |
| `IN_PROGRESS` | `CANCELLED` | `2xx`; status = `CANCELLED` |
| `RESOLVED` | `CLOSED` | `2xx`; status = `CLOSED` |

Also cover happy-path chain: create → `OPEN` → `IN_PROGRESS` → `RESOLVED` → `CLOSED`.

### Denied transitions (must be rejected by the backend)

All other cells in the 5×5 matrix (20 denies), including same-status and all exits from `CLOSED` / `CANCELLED`. **Required examples** (named tests):

| From | To | Test name example |
|------|-----|-------------------|
| `CLOSED` | `OPEN` | `rejectsClosedToOpen` |
| `RESOLVED` | `OPEN` | `rejectsResolvedToOpen` |
| `CANCELLED` | `OPEN` | `rejectsCancelledToOpen` |

### How we prove invalid transitions are rejected by the backend

For each DENY cell:

1. Arrange a ticket in the **from** status (seed via create + allowed transitions, or test fixture helpers that go through the same domain rules).
2. Act: `POST /api/tickets/{id}/transition` with body `{ "status": "<to>" }` (HTTP client in the IT — MockMvc / TestRestTemplate / WebTestClient).
3. Assert:
   - HTTP **`422`**
   - Error `code` = **`INVALID_TRANSITION`**
   - Standard error body fields present (`message`, `path`, etc.)
   - Ticket status **unchanged** on subsequent `GET /api/tickets/{id}`
4. Do **not** mock away the transition policy so the test cannot fail.
5. Do **not** treat UI hiding of illegal buttons as sufficient proof.

Malformed / unknown target status strings → `400` `VALIDATION_ERROR` (separate from the matrix DENY cases).

---

## Search and status filter — how we prove them

| Capability | Spec | Proof |
|------------|------|-------|
| Keyword search | `GET /api/tickets?q=` — case-insensitive substring on **title OR description**; comments not searched | **Integration:** seed tickets with distinct titles/descriptions; assert matching ids in `content`; non-matches absent; empty/`q` whitespace = no search filter |
| Status filter | `GET /api/tickets?status=` — exact `TicketStatus` | **Integration:** seed multiple statuses; filter returns only that status; omit = all; unknown enum → `400` `VALIDATION_ERROR` |
| Combined | `q` AND `status` | **Integration:** one case proving AND semantics |
| Pagination interaction | Filters apply before page | At least one IT if pagination is implemented per contract |

Optional **frontend** tests: list controls update query / displayed rows when API is mocked — confidence only; **backend ITs are the source of truth** for FR6/FR7.

---

## Frontend tests — only where they add confidence

Do **not** over-test. Prefer RTL behaviour tests over snapshots and visual regression.

| Worth testing | Why |
|---------------|-----|
| Create/edit form shows API `message` and maps `details[]` to fields | FR10; easy to regress |
| List search input / status filter triggers the expected request or filtered UI state (with mocked fetch) | UX wiring for FR6/FR7 |
| Illegal transition button hidden **and** that a failed `422` still shows an error if somehow called | Optional; must not replace backend matrix ITs |

| Skip / avoid | Why |
|--------------|-----|
| Pixel layouts, CSS snapshots | Brittle, low signal |
| Re-testing the full 5×5 matrix in the browser | Belongs in backend ITs |
| Exhaustive page object suites for every click | Out of assessment focus |

Manual UI smoke (create, list, detail, update, comment, errors) remains part of “done before Review” below.

---

## No secrets committed — how we prove it

| Check | Mechanism |
|-------|-----------|
| Secret files ignored | `.gitignore` includes `.env`, local credential files, IDE secret stores, and any `application-*-secret.yml` patterns used by the project |
| Examples without values | Committed `.env.example` and/or sample YAML use **placeholders only** (`changeme`, empty, or `${VAR}`) — never real passwords |
| Test config | H2 URL / Testcontainers; no production connection strings or API keys in `src/test` |
| Review / CI hygiene | Before Review: grep/review for hardcoded `password=`, private keys, tokens; optional simple CI script or pre-commit check that fails on common secret patterns |
| If a secret appears | Remove from history if needed, rotate, document in review notes — per project security rules |

This maps to acceptance criterion **“No secrets are committed.”** Proof is repo hygiene + review checklist, not a runtime unit test.

---

## Core Acceptance Criteria → test type and location

| # | Core Acceptance Criterion | Test type | Primary location / proof |
|---|---------------------------|-----------|---------------------------|
| 1 | Ticket can be created from UI | Manual smoke + optional FE form test; API covered by IT | Manual: create flow; `POST /api/tickets` IT/slice |
| 2 | Tickets can be listed | Integration (+ optional FE) | `GET /api/tickets` IT; list page smoke |
| 3 | Ticket details can be viewed | Integration (+ manual) | `GET /api/tickets/{id}` IT; detail page smoke |
| 4 | Ticket fields can be updated | Integration / slice | Update endpoint IT; assert persisted fields |
| 5 | Assignee can be changed | Integration / slice | Update with `assignee`; GET confirms |
| 6 | Comments can be added | Integration | Add-comment endpoint; detail includes comments after reload |
| 7 | Search works | Integration (+ sparse FE) | `GET /api/tickets?q=` IT (see Search section) |
| 8 | Status filter works | Integration (+ sparse FE) | `GET /api/tickets?status=` IT |
| 9 | Valid status transitions work | **State-machine integration** | `TicketStateMachineIT` — all 5 ALLOW edges + happy path |
| 10 | Invalid status transitions rejected by backend | **State-machine integration** | Same suite — all 20 DENY cells; `422` `INVALID_TRANSITION`; status unchanged |
| 11 | Data survives application restart | Integration (+ durable demo) | Persistence IT (new context / durable store); optional manual restart |
| 12 | Backend validation works | Unit + slice + IT | Validation unit; `400` error-body slice/IT for create/update/comment |
| 13 | UI shows meaningful errors | Frontend (targeted) + manual | Form error mapping test; manual check of banner/`details` |
| 14 | State-machine integration tests pass | Integration | Full matrix suite green in default test command |
| 15 | No secrets are committed | Repo / process check | `.gitignore`, env examples without values, review grep |

---

## CI and commands

| Expectation | Detail |
|-------------|--------|
| Default test command | Runs unit + slice + integration (including state-machine IT) |
| Flakes | Fix or quarantine with an explicit note — do not ignore |
| Coverage % | Guide only; **matrix completeness** and acceptance mapping matter more |
| Generate / extend | Follow `commands/generate-tests.md` priority order (state machine first) |

---

## What “done” looks like before Review

Before entering Review (`commands/review-code.md` / Ask workflow), all of the following hold:

1. **Specs stable** — `state-machine.md`, `api-contract.md`, and this strategy agree on transitions, codes (`422` / `INVALID_TRANSITION`), and list query params.
2. **State-machine IT green** — every ALLOW and DENY cell in the 5×5 matrix has a named backend test; required illegal examples (`CLOSED→OPEN`, `RESOLVED→OPEN`, `CANCELLED→OPEN`) present.
3. **Persistence proven** — at least one automated test shows ticket (and comment) data still readable after new context / DB session (or documented durable-store restart proof).
4. **Search & filter proven** — integration tests for `q` and `status` (and one combined case) pass.
5. **Validation & errors proven** — backend rejects invalid payloads with the standard error body; targeted FE or manual check shows meaningful UI errors.
6. **Acceptance map satisfied** — every row in the Core Acceptance Criteria table above has evidence (automated and/or listed manual smoke).
7. **Secrets hygiene** — `.gitignore` and env examples reviewed; no real credentials in repo or test fixtures.
8. **Suite executed** — default test command run locally (or CI); results recorded; no “assumed green.”
9. **AI mistake pass** — generated tests checked against specs (no invented endpoints/statuses); contradictions fixed toward the **spec**.

Gaps remaining after implementation are listed explicitly for Review (missing DENY cases = high severity per `commands/review-code.md`).

---

## Traceability

| Artefact | Relationship |
|----------|----------------|
| `spec/requirements.md` | Core Acceptance Criteria mapped above |
| `spec/state-machine.md` | Matrix copied into state-machine IT |
| `spec/api-contract.md` | Endpoints, query params, error codes asserted in slice/IT |
| `spec/architecture.md` | H2 / Postgres / Testcontainers choices; secrets out of git |
| `spec/ui-flow.md` | FE error display and list search/filter behaviour (sparse FE tests) |
| `rules/testing.md` | Pyramid, anti-patterns, CI expectations |
| `commands/generate-tests.md` | Order of writing suites |
| `commands/review-code.md` | Missing denied-transition tests = high severity |

---

## Out of scope for this strategy

- Performance / load testing
- Security penetration testing beyond secrets-in-repo hygiene
- E2E browser automation of the full matrix (backend ITs own that)
- Auth/session suites (auth not required by requirements)
