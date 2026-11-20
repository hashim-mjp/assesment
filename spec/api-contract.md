# REST API contract — Support Ticket Management System

Derived from `spec/requirements.md`, `spec/data-model.md`, and `rules/api-standards.md`. Status **allow/deny matrix** lives in `spec/state-machine.md` (not yet written); this contract defines **how** status is changed over HTTP and how illegal transitions are rejected.

**No implementation in this document.**

---

## Conventions

| Topic | Choice |
|-------|--------|
| Base URL prefix | `/api` (no version segment for this take-home) |
| Content-Type | `application/json` (request and response) |
| JSON field names | **camelCase** |
| Resource IDs | UUID strings in paths and bodies |
| Timestamps | ISO-8601 UTC (`2026-09-22T09:00:00Z`) |
| Auth | None (not required by requirements) |
| Concurrency | **Last-write-wins** — no `ETag` / version field; concurrent updates overwrite; no `409` for stale writes |

### Enumerations (API values)

**`TicketStatus`:** `OPEN` | `IN_PROGRESS` | `RESOLVED` | `CLOSED` | `CANCELLED`

**`TicketPriority`:** `LOW` | `MEDIUM` | `HIGH` | `URGENT`

Unknown enum strings → `400` with `code: VALIDATION_ERROR` (never treated as “no match” / empty success).

### Shared response types

**Ticket summary** (list items):

| Field | Type | Notes |
|-------|------|--------|
| `id` | string (UUID) | |
| `title` | string | |
| `priority` | `TicketPriority` | |
| `assignee` | string \| `null` | `null` = unassigned |
| `status` | `TicketStatus` | |
| `createdAt` | string (ISO-8601) | |
| `updatedAt` | string (ISO-8601) | |

**Ticket detail** = summary fields plus:

| Field | Type | Notes |
|-------|------|--------|
| `description` | string | |
| `comments` | `Comment[]` | Ordered by `createdAt` ascending |

**Comment:**

| Field | Type | Notes |
|-------|------|--------|
| `id` | string (UUID) | |
| `body` | string | |
| `author` | string \| `null` | Optional |
| `createdAt` | string (ISO-8601) | |

**Paged list envelope:**

```json
{
  "content": [ /* Ticket summary */ ],
  "page": 0,
  "size": 20,
  "totalElements": 0,
  "totalPages": 0
}
```

### Error body (all 4xx / 5xx)

Per `rules/api-standards.md`:

```json
{
  "timestamp": "2026-09-22T09:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "code": "VALIDATION_ERROR",
  "message": "Title must not be blank",
  "path": "/api/tickets",
  "details": [
    { "field": "title", "message": "must not be blank", "rejectedValue": "" }
  ]
}
```

| Field | Role |
|-------|------|
| `message` | Human-readable; UI may show as-is |
| `code` | Machine-readable stable token |
| `details` | Optional field-level list for forms; omit or `[]` when N/A |

**Stable `code` values used by this API:**

| `code` | Typical HTTP |
|--------|----------------|
| `VALIDATION_ERROR` | `400` |
| `NOT_FOUND` | `404` |
| `INVALID_TRANSITION` | `422` |
| `INTERNAL_ERROR` | `500` |

Do not return `200` with an embedded error for expected failures.

### Field limits (backend-enforced)

| Field | Constraint |
|-------|------------|
| `title` | required where noted; non-blank; max **200** chars |
| `description` | required where noted; non-blank; max **10_000** chars |
| `assignee` | optional; max **120** chars when present; blank string treated as `null` (unassigned) |
| `body` (comment) | required; non-blank; max **5_000** chars |
| `author` (comment) | optional; max **120** chars |

---

## Endpoints

### 1. Create ticket

`POST /api/tickets`

**Request body:**

```json
{
  "title": "Cannot reset password",
  "description": "User gets 500 on reset link.",
  "priority": "HIGH",
  "assignee": "alex@example.com"
}
```

| Field | Required | Notes |
|-------|----------|--------|
| `title` | **yes** | Non-blank |
| `description` | **yes** | Non-blank |
| `priority` | no | Default **`MEDIUM`** if omitted |
| `assignee` | no | Default `null` if omitted |
| `status` | — | **Must not** be accepted from client; always created as `OPEN`. If present → `400` `VALIDATION_ERROR` |

**Success:** `201 Created`

- Body: **Ticket detail** (with `comments: []`)
- Header: `Location: /api/tickets/{id}`

**Errors:** `400` validation; `500` unexpected

---

### 2. List tickets (includes search & status filter)

`GET /api/tickets`

Search and status filter are **query parameters on this same list endpoint** (no separate search resource).

| Query param | Required | Default | Meaning |
|-------------|----------|---------|---------|
| `q` | no | _(absent)_ | Keyword search: case-insensitive substring match on **`title` OR `description`**. Empty or whitespace-only → no search filter. Comments are **not** searched. |
| `status` | no | _(absent)_ | Exact `TicketStatus` match. Omit = all statuses. Unknown value → `400` `VALIDATION_ERROR`. |
| `page` | no | `0` | 0-based page index (≥ 0) |
| `size` | no | `20` | Page size; max **100** (larger → `400`) |
| `sort` | no | `createdAt,desc` | Allowed: `createdAt,asc\|desc`, `updatedAt,asc\|desc`. Other values → `400`. |

Filters apply **before** pagination. `q` and `status` may be combined (AND).

**Example:** `GET /api/tickets?q=password&status=OPEN&page=0&size=20`

**Success:** `200 OK` — paged envelope of **Ticket summary** objects

**Errors:** `400` invalid query/enum; `500` unexpected

---

### 3. Get ticket details

`GET /api/tickets/{id}`

**Success:** `200 OK` — **Ticket detail** (includes `comments`)

**Errors:**

| Condition | Status | `code` |
|-----------|--------|--------|
| Unknown `id` | `404` | `NOT_FOUND` |
| Malformed UUID | `400` | `VALIDATION_ERROR` |

---

### 4. Update title, description, priority, assignee

`PATCH /api/tickets/{id}`

Partial update (**merge**): only fields present in the body are changed. **`status` is not updatable here** — use the status transition endpoint (§5).

**Request body** (all fields optional; at least one of the allowed fields must be present):

```json
{
  "title": "Updated title",
  "description": "Updated description",
  "priority": "LOW",
  "assignee": null
}
```

| Field | Semantics |
|-------|-----------|
| omitted | Leave unchanged |
| `title` / `description` / `priority` | Replace with new value (must pass validation) |
| `assignee: null` or `""` | Unassign |
| `assignee: "…"` | Set assignee |
| `status` present | **`400` `VALIDATION_ERROR`** — status changes only via §5 |

**Success:** `200 OK` — **Ticket detail** (comments unchanged / included)

**Errors:** `400` validation / empty body / status in body; `404` unknown id; `500` unexpected

**Concurrency:** last-write-wins (see Conventions).

---

### 5. Status transition (separate from field update)

Status is changed **only** via this endpoint — not via create or `PATCH` (§4).

`POST /api/tickets/{id}/transition`

**Request body:**

```json
{
  "status": "IN_PROGRESS"
}
```

| Field | Required | Notes |
|-------|----------|--------|
| `status` | **yes** | Target `TicketStatus` |

**Behaviour:**

1. Load ticket; if missing → `404` `NOT_FOUND`.
2. If `status` is missing, blank, or not a known enum → `400` `VALIDATION_ERROR`.
3. If current → target is **not** an allowed transition (see `spec/state-machine.md`) → **`422`** with `code: INVALID_TRANSITION`. Do **not** coerce, ignore, or “snap” to another status.
4. If allowed → update `status` and `updatedAt`; return updated ticket.

**Success:** `200 OK` — **Ticket detail**

**Invalid transition error example:**

```json
{
  "timestamp": "2026-09-22T09:00:00Z",
  "status": 422,
  "error": "Unprocessable Entity",
  "code": "INVALID_TRANSITION",
  "message": "Cannot transition from CLOSED to OPEN",
  "path": "/api/tickets/550e8400-e29b-41d4-a716-446655440000/transition",
  "details": [
    {
      "field": "status",
      "message": "transition not allowed",
      "rejectedValue": "OPEN"
    }
  ]
}
```

**Expected allowed paths** (authoritative table in `state-machine.md`):  
`OPEN → IN_PROGRESS → RESOLVED → CLOSED`; `OPEN → CANCELLED`; `IN_PROGRESS → CANCELLED`.  
Illegal examples that **must** yield `422`: `CLOSED → OPEN`, `RESOLVED → OPEN`, `CANCELLED → OPEN` (and any other denied pair).

Same-status request (e.g. `OPEN → OPEN`): **deny** with `422` `INVALID_TRANSITION` (not a no-op success).

---

### 6. Add comment

`POST /api/tickets/{id}/comments`

**Request body:**

```json
{
  "body": "Reproduced on staging.",
  "author": "sam"
}
```

| Field | Required | Notes |
|-------|----------|--------|
| `body` | **yes** | Non-blank |
| `author` | no | Default `null` |

**Success:** `201 Created`

- Body: **Comment**
- Header: `Location: /api/tickets/{id}/comments/{commentId}` (optional but preferred)

**Errors:** `400` validation; `404` ticket not found; `500` unexpected

Comments are append-only; no update/delete endpoints in this contract.

---

## Endpoint summary

| Method | Path | Purpose |
|--------|------|---------|
| `POST` | `/api/tickets` | Create ticket (`status` = `OPEN`) |
| `GET` | `/api/tickets` | List / search (`q`) / filter (`status`) / page |
| `GET` | `/api/tickets/{id}` | Ticket detail + comments |
| `PATCH` | `/api/tickets/{id}` | Update title, description, priority, assignee |
| `POST` | `/api/tickets/{id}/transition` | Status transition (validated) |
| `POST` | `/api/tickets/{id}/comments` | Add comment |

Out of scope: delete ticket, edit/delete comment, auth, bulk operations.

---

## Traceability

| Requirement | Contract coverage |
|-------------|-------------------|
| FR1 Create | `POST /api/tickets` |
| FR2 List | `GET /api/tickets` |
| FR3 Detail | `GET /api/tickets/{id}` |
| FR4 Update fields / assignee | `PATCH /api/tickets/{id}` |
| FR5 Comments | `POST /api/tickets/{id}/comments` + detail `comments` |
| FR6 Search | `q` on list |
| FR7 Status filter | `status` on list |
| FR9 Backend validation | `400` + error body; transition checks |
| FR10 UI errors | `message` + `code` + optional `details` |
| Status lifecycle | `POST .../transition` → `422` `INVALID_TRANSITION` on deny |

---

## Locked decisions (were open in data-model)

| Topic | Decision in this contract |
|-------|---------------------------|
| ID type | UUID string |
| Priority enum | `LOW` \| `MEDIUM` \| `HIGH` \| `URGENT`; default `MEDIUM` on create |
| Search fields | `title` and `description` via `q` |
| List pagination | Paged envelope (not bare array) |
| Status change | Dedicated `POST .../transition`; not via `PATCH` |
| Invalid transition HTTP | `422` + `INVALID_TRANSITION` |
| Unassign | `assignee: null` or `""` on `PATCH` |
| Concurrency | Last-write-wins |
