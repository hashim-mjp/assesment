# UI flow — Support Ticket Management System

Derived from `spec/requirements.md`, `spec/architecture.md`, and `spec/api-contract.md`. Status **allow/deny** details live in `spec/state-machine.md`; this document defines how the frontend presents actions and handles API outcomes.

**No application code in this document.** Stack: React / Next.js (or equivalent). UX stays deliberately small: **one list view**, **one detail view**, **create/edit forms**, and a **comment thread** on detail.

---

## Design principles

| Principle | Rule |
|-----------|------|
| Thin client | UI calls REST endpoints from `api-contract.md`; it does not invent business rules |
| Meaningful errors | On 4xx/5xx, show API `message`; for forms, also map `details[]` to fields when present (FR10) |
| Status transitions | UI **may** hide or disable illegal target statuses for convenience; it **must not** be the only guard. Backend still rejects illegal transitions with `422` `INVALID_TRANSITION` |
| Simple IA | No dashboards, no separate search page, no comment-only routes |

### Error display convention

| Source | UI treatment |
|--------|----------------|
| `message` | Primary banner / toast / inline alert (human-readable) |
| `details[].field` + `details[].message` | Field-level hint under the matching form control |
| `code` | Optional for logging / conditional copy; do not show raw codes as the only message |
| Network / parse failure | Generic “Could not reach the server” (or similar); do not claim success |

Do **not** treat failed requests as silent no-ops.

---

## Screens and navigation

Suggested routes (from `architecture.md`):

| Route | Screen | Purpose |
|-------|--------|---------|
| `/tickets` (or `/`) | **Ticket list** | List, search (`q`), status filter, entry to create/detail |
| `/tickets/new` | **Create ticket** | Form → `POST /api/tickets` |
| `/tickets/[id]` | **Ticket detail** | View fields, edit form, status actions, comment thread |

```
/tickets  ──[New ticket]──►  /tickets/new  ──[Create success]──►  /tickets/{id}
    │                              │
    │                              └──[Cancel]──►  /tickets
    │
    └──[Row / Open]──►  /tickets/[id]
                            │
                            ├── edit fields in place (or short edit section)
                            ├── status transition controls
                            └── comment thread + add form
```

**URL sync (list):** Prefer reflecting search and filter in query params (`?q=` / `?status=`) so refresh and share preserve state. Pagination may use `page` / `size` if implemented.

Out of scope for UI: delete ticket, edit/delete comments, auth screens, bulk actions.

---

## Shared UI states

### Loading

- Disable primary submit buttons while a mutation is in flight.
- List/detail: show a lightweight loading indicator until the first successful response (or error).

### Empty

| Context | Message (intent) |
|---------|------------------|
| List, no filters | “No tickets yet. Create one to get started.” + link to `/tickets/new` |
| List, with `q` and/or `status` | “No tickets match your search or filter.” + clear controls |
| Detail comments | “No comments yet.” (thread header still visible; add form available) |

### Success

| Action | Feedback |
|--------|----------|
| Create | Navigate to detail of new ticket; optional short success toast |
| Update fields | Form shows updated values from response body; optional toast |
| Transition | Status badge updates from response; optional toast |
| Add comment | New comment appears at end of thread; form clears |

### Error (global)

| HTTP / `code` | Typical UI |
|---------------|------------|
| `400` `VALIDATION_ERROR` | Banner with `message` + field errors from `details` |
| `404` `NOT_FOUND` | Detail: full-page “Ticket not found” + link back to list |
| `422` `INVALID_TRANSITION` | Alert with `message` (e.g. “Cannot transition from CLOSED to OPEN”); status unchanged |
| `500` `INTERNAL_ERROR` | Banner: server error / try again; do not clear form data needlessly |

---

## Flow: List tickets

**Screen:** Ticket list.

**Layout:** Table or simple list of summaries (`id`, `title`, `priority`, `assignee`, `status`, `createdAt`). Controls: keyword search, status filter, “New ticket”, row click → detail. Optional pagination controls if `totalPages > 1`.

| Step | User action | API call | UI result |
|------|-------------|----------|-----------|
| 1 | Open `/tickets` | `GET /api/tickets?page=0&size=20` (defaults per contract) | **Success `200`:** render `content`. **Empty** (`totalElements = 0`): empty state. **Error:** list error banner |
| 2 | Change page | `GET /api/tickets?...&page={n}` | Replace rows with new page |

**Empty / error / success:** covered in Shared UI states. List does not need a separate “success” toast for the initial load.

---

## Flow: Search by keyword

**Screen:** Ticket list (same view).

| Step | User action | API call | UI result |
|------|-------------|----------|-----------|
| 1 | Type keyword and submit / debounce apply | `GET /api/tickets?q={keyword}&…` (plus current `status`/`page` if set) | **`200`:** matching summaries. **Empty match:** filtered empty state. Reset `page` to `0` when `q` changes |
| 2 | Clear search | `GET /api/tickets` without `q` (keep `status` if any) | Full (or status-filtered) list again |

**Notes:** Search is case-insensitive on **title OR description** (not comments). Empty/whitespace `q` behaves as no search filter (contract).

**Errors:** `400` if somehow an invalid query is sent → show `message`. Do not clear the keyword input on error.

---

## Flow: Filter by status

**Screen:** Ticket list (same view).

| Step | User action | API call | UI result |
|------|-------------|----------|-----------|
| 1 | Choose a status from filter control (`OPEN`, `IN_PROGRESS`, …) | `GET /api/tickets?status={STATUS}&…` (combine with `q` if set) | **`200`:** filtered list. **Empty:** filtered empty state. Reset `page` to `0` when status changes |
| 2 | Choose “All” / clear filter | `GET /api/tickets` without `status` | Unfiltered (or search-only) list |

**Errors:** Selecting a value outside the enum should be impossible in the UI; if the API returns `400` `VALIDATION_ERROR` for an unknown `status`, show `message`.

Search and status filter **AND** together when both are set.

---

## Flow: Create ticket

**Screen:** Create form (`/tickets/new`).

**Fields:** `title` (required), `description` (required), `priority` (optional; default UI hint `MEDIUM`), `assignee` (optional). Do **not** offer a status control — create always yields `OPEN` on the server.

| Step | User action | API call | UI result |
|------|-------------|----------|-----------|
| 1 | Fill form and submit | `POST /api/tickets` with body `{ title, description, priority?, assignee? }` | **`201`:** navigate to `/tickets/{id}` with returned detail (`comments: []`). **`400`:** stay on form; show `message` + field `details`. **`500`:** stay on form; show server error |
| 2 | Cancel | — | Navigate back to `/tickets` without calling the API |

**Client-side checks (optional UX only):** blank title/description can be blocked before submit; backend remains authoritative (FR9).

**Empty:** N/A (form). **Success:** navigation + optional toast. **Error:** inline validation as above.

---

## Flow: View ticket details

**Screen:** Ticket detail (`/tickets/[id]`).

**Layout:** Header (title, status badge, priority, assignee, timestamps); description; **edit section** (or editable fields); **status transition** controls; **comment thread** + add-comment form. Link/back to list.

| Step | User action | API call | UI result |
|------|-------------|----------|-----------|
| 1 | Open ticket from list (or deep link) | `GET /api/tickets/{id}` | **`200`:** render detail + comments (oldest → newest). **`404`:** not-found page + link to list. **`400`** (bad id): validation message + link to list. **`500`:** error state with retry |

**Empty comments:** “No comments yet.” while still showing the add form.

---

## Flow: Update title, description, priority, assignee

**Screen:** Ticket detail — edit form / editable section.

**Fields:** same as create for the four updatable fields. Status is **not** part of this form (use transition controls).

| Step | User action | API call | UI result |
|------|-------------|----------|-----------|
| 1 | Change one or more fields and save | `PATCH /api/tickets/{id}` with only changed fields (at least one) | **`200`:** refresh displayed fields from response body. **`400`:** keep edits; show `message` + field errors. **`404`:** not-found. **`500`:** keep edits; show error |
| 2 | Clear assignee | `PATCH` with `"assignee": null` (or `""` per contract) | **`200`:** show unassigned |

**Concurrency:** last-write-wins — no conflict UI required.

Do not send `status` in `PATCH`; if the API returns `400` for that mistake, treat as a client bug and show `message`.

---

## Flow: Status transition

**Screen:** Ticket detail — status actions (buttons, select of **allowed** targets, or similar).

**UX vs enforcement (required call-out):**

- The UI **may** only offer targets allowed for the current status (per `state-machine.md`) to reduce mistakes.
- The UI **must not** be the only guard. A stale tab, a crafted request, or a mismatched client matrix can still hit the API; the backend **rejects** illegal transitions with **`422`** `INVALID_TRANSITION`. On that response, **do not** change the displayed status; show `message`.

Expected happy path (authoritative table in `state-machine.md`):  
`OPEN → IN_PROGRESS → RESOLVED → CLOSED`; `OPEN → CANCELLED`; `IN_PROGRESS → CANCELLED`. Terminal: `CLOSED`, `CANCELLED`.

| Step | User action | API call | UI result |
|------|-------------|----------|-----------|
| 1 | Choose an allowed target status and confirm | `POST /api/tickets/{id}/transition` `{ "status": "<TARGET>" }` | **`200`:** update status badge / controls from response. **`422` `INVALID_TRANSITION`:** alert with `message`; status unchanged; refresh detail optional. **`404`:** not-found. **`400`:** show validation `message` |
| 2 | (Edge) Illegal target somehow submitted | same | Same **`422`** handling — proves backend is source of truth |

Same-status requests are denied by the API (`422`); UI should not offer “transition to current status.”

---

## Flow: Add comments

**Screen:** Ticket detail — comment thread + form.

**Fields:** `body` (required), `author` (optional). Comments are append-only (no edit/delete UI).

| Step | User action | API call | UI result |
|------|-------------|----------|-----------|
| 1 | Submit comment | `POST /api/tickets/{id}/comments` `{ body, author? }` | **`201`:** append returned comment to thread; clear `body` (keep `author` if desired). **`400`:** show `message` / field error on `body`; keep draft. **`404`:** ticket gone → not-found. **`500`:** keep draft; show error |

**Empty:** empty thread copy before first comment. **Success:** new item at end (matches API order: `createdAt` ascending).

Optional: after success, either trust the `201` body or re-fetch `GET /api/tickets/{id}` — both acceptable; prefer appending the `201` body for snappiness.

---

## Flow: Display meaningful errors (cross-cutting)

Maps FR10 and Core Acceptance Criteria (“UI shows meaningful errors”).

| Trigger | API | UI result |
|---------|-----|-----------|
| Blank title on create/update | `400` `VALIDATION_ERROR` | Show `message` (e.g. “Title must not be blank”) and underline `title` from `details` |
| Unknown enum / bad query | `400` | Show `message`; do not empty the page silently |
| Missing ticket | `404` `NOT_FOUND` | Dedicated not-found state |
| Illegal status change | `422` `INVALID_TRANSITION` | Prominent alert with API `message` (e.g. “Cannot transition from CLOSED to OPEN”) |
| Server failure | `500` | Generic failure copy; preserve user input on forms |

Prefer API `message` over hard-coded guesses so backend wording stays authoritative.

---

## Acceptance mapping (UI)

| Acceptance criterion | Screen / flow |
|----------------------|---------------|
| Ticket created from UI | Create flow → `POST /api/tickets` |
| Tickets listed | List → `GET /api/tickets` |
| Details viewed | Detail → `GET /api/tickets/{id}` |
| Fields / assignee updated | Detail edit → `PATCH /api/tickets/{id}` |
| Comments added | Comment form → `POST /api/tickets/{id}/comments` |
| Search works | List `q` → `GET /api/tickets?q=` |
| Status filter works | List `status` → `GET /api/tickets?status=` |
| Valid transitions work | Detail → `POST .../transition` → `200` |
| Invalid transitions rejected by backend | Detail still surfaces `422`; UI hide is optional only |
| UI shows meaningful errors | Error convention + flows above |

---

## Traceability

| Requirement | UI-flow coverage |
|-------------|------------------|
| FR1 Create | Create screen + flow |
| FR2 List | List screen + flow |
| FR3 Detail | Detail screen + flow |
| FR4 Update fields / assignee | Edit on detail → `PATCH` |
| FR5 Comments | Comment thread + add flow |
| FR6 Search | List keyword control → `q` |
| FR7 Status filter | List filter → `status` |
| FR9 Backend validation | UI optional checks only; errors from API |
| FR10 Meaningful errors | Error convention + validation / transition / not-found |
| Status lifecycle | Transition controls + **backend remains authoritative** |
