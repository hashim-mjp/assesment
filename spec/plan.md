# Implementation plan — Support Ticket Management System

**Plan only.** Do not build the full application in one pass. Implement **one task per prompt**, mark it done, then move to the next.

Derived from `spec/requirements.md`, `spec/architecture.md`, `spec/data-model.md`, `spec/api-contract.md`, `spec/state-machine.md`, `spec/ui-flow.md`, `spec/test-strategy.md`, and steering files under `rules/`.

**Assumed layout** (sibling apps, per architecture):

| Path | Role |
|------|------|
| `backend/` | Java 21 + Spring Boot |
| `frontend/` | React / Next.js |
| Repo root | `.gitignore`, `.env.example`, README |

Adjust package/root names only if an earlier task already fixed them — stay consistent.

---

## How to use this plan

1. Pick the next unchecked task (`T01` …).
2. Implement **only** that task (see `prompts/09-backend-implementation.md` / `prompts/10-frontend-implementation.md`).
3. Run the task’s **Verification** step.
4. Check the box; do not skip ahead unless a dependency note says otherwise.
5. After `T12`–`T15` suites are green, run the “Done before Review” checklist at the end.

---

## Task sequence

### T01 — Backend project skeleton (no business features)

- [x] Complete

| | |
|--|--|
| **Goal** | Runnable Spring Boot app with layered packages, profiles, and secrets hygiene placeholders. No ticket APIs yet. |
| **Specs** | `architecture.md` (modules, profiles, secrets); `requirements.md` (stack); `rules/java-springboot.md` |
| **Files likely touched** | `backend/pom.xml` or `build.gradle*`; `backend/src/main/java/.../Application.java`; empty packages `api/`, `application/`, `domain/`, `persistence/`, `config/`; `application.yml` + `application-local.yml` / `application-postgres.yml` (placeholders only); root `.gitignore`; `.env.example` |
| **Verification** | `./mvnw spring-boot:run` (or Gradle equivalent) starts on `:8080` with health or empty context; no real DB passwords in git; `.env` ignored |

---

### T02 — Frontend project skeleton (no business features)

- [x] Complete

| | |
|--|--|
| **Goal** | Runnable Next.js (or React) app with routes stubs and API base URL from env. No real ticket UI yet. |
| **Specs** | `architecture.md` (routes); `ui-flow.md` (route names); secrets NFR |
| **Files likely touched** | `frontend/package.json`; App Router stubs for `/tickets`, `/tickets/new`, `/tickets/[id]`; `lib/api.ts` (base URL only); `frontend/.env.example`; root `.gitignore` updates (`node_modules`, `.env.local`) |
| **Verification** | `npm run dev` loads placeholder pages; API URL comes from env, not hardcoded secrets |

---

### T03 — Ticket entity + migrations / schema

- [x] Complete

| | |
|--|--|
| **Goal** | Persist `Ticket` with enums, timestamps, indexes; durable profile documented. Comments table deferred to T07. |
| **Specs** | `data-model.md` (Ticket, enums, indexes); `architecture.md` (H2 file / Postgres); `api-contract.md` (UUID, priority enum) |
| **Files likely touched** | `domain/Ticket.java`, `TicketStatus.java`, `TicketPriority.java`; `persistence/TicketRepository.java`; Flyway/Liquibase `V1__ticket.sql` **or** documented `ddl-auto` + entity; `application-*.yml` datasource placeholders |
| **Verification** | App starts against local H2 file or Postgres; schema has `ticket` with status/priority columns and `idx_ticket_status` / `created_at` indexes (or equivalent); restart process and confirm DB file/volume still exists |

---

### T04 — Create + list + get ticket API (no search/filter/patch yet)

- [x] Complete

| | |
|--|--|
| **Goal** | `POST /api/tickets`, basic `GET /api/tickets` (paged, default sort), `GET /api/tickets/{id}` without comments payload complexity (empty `comments: []` OK). |
| **Specs** | `api-contract.md` §§1–3 (create/list/get; create → `OPEN`; no client status); FR1–FR3; `rules/api-standards.md` |
| **Files likely touched** | `api/TicketController.java`; request/response DTOs/records; `application/TicketService.java`; repository findAll paging; minimal CORS for local if needed |
| **Verification** | Manual/curl: create returns `201` + `Location` + status `OPEN`; list returns paged envelope; get by id works; unknown id → still may be raw 404 until T10 — note gap |

---

### T05 — Update fields + assignee (`PATCH`)

- [x] Complete

| | |
|--|--|
| **Goal** | Partial update of title, description, priority, assignee; reject `status` in body. |
| **Specs** | `api-contract.md` §4; FR4; `data-model.md` updatable fields |
| **Files likely touched** | `PatchTicketRequest` DTO; controller `PATCH`; service merge logic; `updatedAt` bump |
| **Verification** | curl: patch one field; unassign with `null`/`""`; body containing `status` rejected (`400` once T10 exists — at minimum reject now); GET shows persisted changes |

---

### T06 — Status transition endpoint + domain state machine

- [x] Complete

| | |
|--|--|
| **Goal** | `POST /api/tickets/{id}/transition` enforcing the full allow/deny matrix. Prefer implementing policy + HTTP together so invalid transitions already return domain failure (map to `422` in T10 if advice not ready). |
| **Specs** | `state-machine.md` (full 5×5); `api-contract.md` §5; FR status lifecycle |
| **Files likely touched** | `domain/TicketStatusTransitions.java` (or equivalent policy); service `transition`; controller endpoint; optional domain exception `InvalidTransitionException` |
| **Verification** | Happy path `OPEN → IN_PROGRESS → RESOLVED → CLOSED`; `OPEN → CANCELLED`; deny `CLOSED → OPEN` (assert status unchanged). Full matrix automation deferred to T12 |

---

### T07 — Comments entity + add-comment API

- [x] Complete

| | |
|--|--|
| **Goal** | `Comment` persistence + `POST /api/tickets/{id}/comments`; detail GET includes comments ascending. |
| **Specs** | `data-model.md` (Comment, FK); `api-contract.md` §3, §6; FR5 |
| **Files likely touched** | `Comment` entity; migration `V2__comment.sql`; `CommentRepository`; service/controller for comments; ticket detail mapping includes `comments` |
| **Verification** | Add comment → `201`; GET ticket shows comment; missing ticket → `404` (after T10); restart still shows comment (spot-check) |

---

### T08 — Search (`q`) and status filter on list

- [x] Complete

| | |
|--|--|
| **Goal** | List endpoint supports `q` (title OR description, case-insensitive) and `status`, AND-combined, before pagination. |
| **Specs** | `api-contract.md` §2; `data-model.md` indexes/search notes; FR6–FR7 |
| **Files likely touched** | Repository query methods / Specifications / JPQL; controller query params; validation for unknown `status`, `size` max 100, `sort` whitelist |
| **Verification** | Seed tickets; `?q=` matches title/description only; `?status=OPEN` filters; combined AND; empty `q` = no search filter; bad enum → `400` once T10 maps errors |

---

### T09 — Backend validation on write DTOs (Bean Validation)

- [x] Complete

| | |
|--|--|
| **Goal** | Enforce field limits and required fields on create/patch/comment/transition bodies at the API boundary. |
| **Specs** | `api-contract.md` field limits; FR9; `rules/java-springboot.md` validation |
| **Files likely touched** | DTO annotations (`@NotBlank`, `@Size`, etc.); create rejects client `status`; empty PATCH body rejected |
| **Verification** | Blank title → rejected; oversized body → rejected; unknown priority string → rejected; do not rely on UI |

---

### T10 — Error mapping (`@RestControllerAdvice`)

- [x] Complete

| | |
|--|--|
| **Goal** | Stable error JSON for all 4xx/5xx; map domain exceptions to contract codes. |
| **Specs** | `api-contract.md` error body + codes; `rules/api-standards.md`; FR9–FR10; `architecture.md` validation/error mapping |
| **Files likely touched** | `api/ApiExceptionHandler.java` (or Advice); exception types (`NotFound`, `InvalidTransition`); ensure `422` + `INVALID_TRANSITION`, `404` + `NOT_FOUND`, `400` + `VALIDATION_ERROR`, `500` + `INTERNAL_ERROR` |
| **Verification** | Sample each code with curl; body has `timestamp`, `status`, `error`, `code`, `message`, `path`; optional `details[]` on validation |

---

### T11 — UI: list + create + detail wired to API

- [x] Complete

| | |
|--|--|
| **Goal** | Wire the three screens to live endpoints; show loading/empty/error basics. Search/filter/status/comments can be minimal stubs if split — prefer completing list/create/detail CRUD shell here. |
| **Specs** | `ui-flow.md` (list, create, detail, errors); `architecture.md` routes; FR1–FR3, FR10 |
| **Files likely touched** | `frontend/app/tickets/page.tsx` (or Pages equiv.); `tickets/new/page.tsx`; `tickets/[id]/page.tsx`; API client helpers; shared error alert component |
| **Verification** | Manual: create from UI → land on detail; list shows ticket; open detail; API errors show `message` (not silent) |

---

### T12 — UI: edit fields, assignee, comments, search, filter, transitions

- [x] Complete

| | |
|--|--|
| **Goal** | Complete remaining UI flows against the API. |
| **Specs** | `ui-flow.md` (edit, comments, search, filter, transition); FR4–FR7; status UX may hide illegal targets but must surface `422` |
| **Files likely touched** | Detail edit form; comment thread; list `q`/`status` query-param sync; transition controls |
| **Verification** | Manual smoke of Core Acceptance Criteria rows for UI (create through filter + comment + valid transition + visible error on forced illegal transition) |

---

### T13 — Tests: domain unit + API slice + state-machine IT

- [x] Complete

| | |
|--|--|
| **Goal** | First automated suite wave per `test-strategy.md`: unit transition policy; WebMvc/slice contract + error bodies; **full** state-machine integration matrix. |
| **Specs** | `test-strategy.md` (unit, slice, state-machine IT); `state-machine.md` table; `rules/testing.md`; `commands/generate-tests.md` |
| **Files likely touched** | `.../domain/TicketStatusTransitionTest.java`; `.../api/TicketControllerWebMvcTest.java`; `.../integration/TicketStateMachineIT.java`; test `application.yml` (H2 or Testcontainers, no secrets) |
| **Verification** | Default test command green; **25** matrix cells covered (5 ALLOW + 20 DENY); named tests for `CLOSED→OPEN`, `RESOLVED→OPEN`, `CANCELLED→OPEN`; DENY asserts `422`, `INVALID_TRANSITION`, status unchanged |

---

### T14 — Tests: search/filter IT + persistence restart + sparse FE

- [x] Complete

| | |
|--|--|
| **Goal** | Prove FR6–FR8 and targeted FR10 UI error mapping. |
| **Specs** | `test-strategy.md` (search/filter, persistence, frontend sparse); acceptance map rows 7–8, 11, 13 |
| **Files likely touched** | `TicketSearchFilterIT.java`; `TicketPersistenceIT.java` (new context / durable store pattern); optional `CreateTicketForm.test.tsx` / list filter test |
| **Verification** | Search + status + combined AND ITs pass; persistence IT proves ticket+comment reload after new context/session; FE test (if added) maps `details[]` — or document manual-only with reason |

---

### T15 — Persistence restart check (durable profile demo)

- [x] Complete

| | |
|--|--|
| **Goal** | Human-verifiable restart survival with durable config (Postgres or file H2). Complements T14 automation. |
| **Specs** | `architecture.md` (restart); `requirements.md` FR8 + acceptance; `test-strategy.md` manual supplementary |
| **Files likely touched** | README runbook section; confirm `application-local.yml` / compose uses file H2 or Postgres; no code unless config was wrong |
| **Verification** | Create ticket + comment via API or UI → stop app → start app → data still listed/detail intact |

---

### T16 — Secret hygiene check

- [x] Complete

| | |
|--|--|
| **Goal** | Repo proves “no secrets committed.” |
| **Specs** | `architecture.md` secrets; `test-strategy.md` secrets section; `requirements.md` NFR; `rules/java-springboot.md` |
| **Files likely touched** | `.gitignore`; `.env.example`; sample YAML placeholders; remove any accidental credentials |
| **Verification** | Grep for `password=`, API keys, private keys; confirm ignored patterns; examples have placeholders only; record result for Review |

---

## Dependency sketch

```
T01 ──► T03 ──► T04 ──► T05 ──► T06
                │         │
                │         └──► T09 ──► T10
                │
                └──► T07 ──► T08
T02 ──────────────────────────► T11 ──► T12
T06 + T10 ────────────────────► T13
T07 + T08 + T10 ──────────────► T14
T03 durable config ───────────► T15
T01/T02 hygiene ──────────────► T16 (final pass anytime after skeletons)
```

Suggested prompt order: **T01 → T02 → T03 → T04 → T05 → T06 → T07 → T08 → T09 → T10 → T11 → T12 → T13 → T14 → T15 → T16**.

If a prompt must stay tiny, split further (e.g. T06 policy-only then HTTP; T11 list-only then create/detail) but keep the same sequence.

---

## Requirements / acceptance traceability

| Acceptance / FR | Tasks |
|-----------------|-------|
| FR1 Create / Ticket created from UI | T04, T11 |
| FR2 List | T04, T11 |
| FR3 Detail | T04, T07, T11 |
| FR4 Update fields / assignee | T05, T12 |
| FR5 Comments | T07, T12 |
| FR6 Search | T08, T12, T14 |
| FR7 Status filter | T08, T12, T14 |
| Valid / invalid transitions | T06, T12, T13 |
| FR8 Persistence / restart | T03, T14, T15 |
| FR9 Backend validation | T09, T10, T13 |
| FR10 Meaningful UI errors | T10, T11, T12, T14 |
| State-machine ITs pass | T13 |
| No secrets committed | T01, T02, T16 |

---

## Done before Review

Mirror `spec/test-strategy.md` “done” list:

- [x] All tasks T01–T16 complete (or explicitly deferred with reason)
- [x] Specs and code agree on transitions, `422` / `INVALID_TRANSITION`, list query params
- [x] State-machine IT: full 5×5 green
- [x] Search, filter, persistence ITs green
- [ ] Manual UI smoke against acceptance checklist in `requirements.md` (operator; FE automated suite deferred — see `frontend/lib/fe-tests.manual.md`)
- [x] Secret hygiene check recorded (`spec/secret-hygiene-check.md`)
- [x] Default test command executed (not assumed green)

Then proceed to Review (`commands/review-code.md` / `prompts/12-review-spec-and-code.md`).

---

## Out of scope (do not plan into tasks)

- Auth / multi-user identity
- Delete ticket / edit-delete comments
- Bulk operations, dashboards, E2E browser matrix
- Inventing product rules beyond locked specs
