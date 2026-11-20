# Data model — Support Ticket Management System

Derived from `spec/requirements.md`. Field-level API shapes live in `spec/api-contract.md`; status **transitions** live in `spec/state-machine.md`. This file defines persistence entities, constraints, and indexes.

---

## Design choices (kept simple)

| Choice | Decision | Rationale |
|--------|----------|-----------|
| Assignee | **Plain string** on `Ticket` (`assignee`) | Requirements need assign/change assignee; no auth or user directory is required |
| Comment author | **Optional plain string** (`author`) | Comments need a body and ticket link; author is useful but not mandated as a User entity |
| User table | **Not introduced** | Avoid inventing identity/product rules absent from the assignment |
| IDs | UUID (preferred) or DB-generated BIGINT | Opaque IDs for REST; pick one in implementation and keep it consistent in the API contract |
| Timestamps | `created_at`, `updated_at` in UTC | Align with API standards (ISO-8601 UTC) |

---

## Enumerations

### `TicketStatus`

Assignment-aligned values (transition rules → `state-machine.md`):

| Value | Notes |
|-------|--------|
| `OPEN` | Initial status on create (default unless `state-machine.md` justifies otherwise) |
| `IN_PROGRESS` | |
| `RESOLVED` | |
| `CLOSED` | Terminal (per state-machine spec) |
| `CANCELLED` | Terminal (per state-machine spec) |

Stored as string/enum column; **not** free text.

### `TicketPriority`

Requirements require a priority field but **do not** define allowed values.

**Open question:** exact enum set (e.g. `LOW` / `MEDIUM` / `HIGH` / `URGENT`) and whether priority is required on create. Until decided in API/data follow-up, treat priority as a **constrained enum** (not arbitrary string) and document the chosen set in `api-contract.md` when locked.

---

## Entity: `Ticket`

| Field | Type | Required | Unique | Notes |
|-------|------|----------|--------|-------|
| `id` | UUID or BIGINT | yes (generated) | yes (PK) | Opaque resource id |
| `title` | VARCHAR(200) | **yes** | no | Non-blank; max length enforced backend |
| `description` | TEXT | **yes** | no | Non-blank; max length TBD (open) |
| `priority` | ENUM / VARCHAR | **open** | no | See `TicketPriority`; updatable (FR4) |
| `assignee` | VARCHAR(120) | **no** | no | Nullable/blank = unassigned; updatable (FR4) |
| `status` | `TicketStatus` | **yes** | no | Set by backend on create; changed only via allowed transitions |
| `created_at` | TIMESTAMP WITH TIME ZONE | yes | no | Set on insert; immutable |
| `updated_at` | TIMESTAMP WITH TIME ZONE | yes | no | Updated on any successful ticket field/status change |

### Ticket rules

- **Create (minimum):** `title`, `description` required (FR1). Priority/assignee may be set on create or later (FR1/FR4) — exact create payload in `api-contract.md`.
- **Status** is never trusted from the client as an arbitrary write without transition checks (domain enforcement).
- Deleting tickets is **out of scope** unless later requirements add it.

### Relationships

- `Ticket` **1 — N** `Comment` (a ticket has many comments; a comment belongs to exactly one ticket).
- Cascade: deleting a ticket is not required; if delete is never exposed, orphan policy is N/A. If delete is added later, prefer cascade-delete comments or restrict — **open**.

---

## Entity: `Comment`

| Field | Type | Required | Unique | Notes |
|-------|------|----------|--------|-------|
| `id` | UUID or BIGINT | yes (generated) | yes (PK) | |
| `ticket_id` | FK → `Ticket.id` | **yes** | no | Parent ticket; reject create if ticket missing (`404`) |
| `body` | TEXT | **yes** | no | Non-blank; max length TBD (open) |
| `author` | VARCHAR(120) | **no** | no | Optional display name/string; no FK to User |
| `created_at` | TIMESTAMP WITH TIME ZONE | yes | no | Set on insert; comments are append-only unless later specs say otherwise |
| `updated_at` | TIMESTAMP WITH TIME ZONE | no | no | Omit unless edits are required (not in requirements) |

### Comment rules

- Comments are listed as part of ticket detail (FR3, FR5).
- No requirement for comment edit/delete in `requirements.md`.

### Relationships

- Many comments → one ticket (`ticket_id` NOT NULL, FK).

---

## ER sketch

```
Ticket 1 ───────────< Comment
  id (PK)                id (PK)
  title                  ticket_id (FK, NOT NULL)
  description            body
  priority               author (optional)
  assignee               created_at
  status
  created_at
  updated_at
```

No `User` entity in this model.

---

## Uniqueness summary

| Constraint | Entity | Columns |
|------------|--------|---------|
| Primary key | Ticket | `id` |
| Primary key | Comment | `id` |
| Foreign key | Comment | `ticket_id` → `Ticket.id` |

No unique constraint on `title` or `assignee` (duplicate titles allowed unless product says otherwise — **not** specified).

---

## Indexes (search & filter)

Aligned with FR6 (keyword search) and FR7 (status filter). Exact match fields for `q`/`search` are confirmed in `api-contract.md`; recommended defaults: **title** and **description**.

| Index | Columns | Purpose |
|-------|---------|---------|
| `idx_ticket_status` | `status` | Efficient **filter by status** (`WHERE status = ?`) |
| `idx_ticket_created_at` | `created_at DESC` | Default list ordering (newest first) |
| `idx_comment_ticket_id` | `ticket_id` | Load comments for ticket detail |
| `idx_ticket_title` | `title` | Assist keyword search on title (B-tree; helps equality/prefix depending on DB) |

### Keyword search notes

- Typical implementation: case-insensitive match on `title` and/or `description` (e.g. SQL `ILIKE` / `LOWER(column) LIKE`).
- Optional composite or expression indexes (e.g. PostgreSQL `pg_trgm` / full-text) are **performance enhancements**, not required by the assignment — call out if adopted.
- Combining `status` filter + keyword: apply both predicates, then paginate (`rules/api-standards.md`).
- Suggested composite when both filters are common: `(status, created_at DESC)` — optional.

H2 local/dev: B-tree indexes above remain valid; advanced Postgres-only index types stay behind the Postgres profile.

---

## Validation vs persistence

| Rule | Enforced in |
|------|-------------|
| Non-null/non-blank title, description, comment body | Bean Validation + NOT NULL columns |
| Status enum / priority enum membership | DB check or JPA enum + API validation |
| Legal status transitions | **Domain/application** (not DB CHECK of all pairs) |
| FK integrity for comments | Database FK |

---

## Open questions

Do **not** invent product rules for these until specified:

1. **Priority enum values** and whether priority is required on create.
2. **Max lengths** for `title`, `description`, `body`, `assignee`, `author`.
3. **Keyword search fields** — title only vs title+description (and whether comments are searchable).
4. **ID strategy** — UUID vs BIGINT.
5. **Assignee/author format** — free text only, or future link to identity.
6. **Comment mutability** — append-only vs edit/delete.
7. **Unassign** — empty string vs `null` semantics in API.
8. **Default sort** for list — assume `created_at,desc` unless contract says otherwise.
9. **Concurrent updates** — last-write-wins is acceptable unless optimistic locking is added later.

---

## Traceability

| Requirement | Data-model coverage |
|-------------|---------------------|
| FR1 create | Ticket required fields |
| FR2 list | Ticket table + list indexes |
| FR3 detail | Ticket + Comments via `ticket_id` |
| FR4 update fields/assignee | Updatable ticket columns |
| FR5 comments | Comment entity + FK |
| FR6 search | Title/description + search indexes |
| FR7 status filter | `status` column + `idx_ticket_status` |
| FR8 persist | Relational tables in durable DB (see `architecture.md`) |
