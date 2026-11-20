---
description: REST API conventions — status codes, error body, pagination and filtering
globs: "**/*.{java,ts,tsx,js,jsx,yml,yaml}"
alwaysApply: false
---

# API Standards

Reusable REST conventions for this repository. Detailed ticket endpoints live in `spec/api-contract.md`; this file is the cross-cutting contract those specs must follow.

## General

- Style: **resource-oriented REST** over HTTP/JSON (`application/json`).
- Versioning: prefer URL prefix (`/api/v1/...`) once versioning starts; until then keep a single stable `/api/...` prefix and document it in the contract.
- Naming: plural nouns for collections (`/tickets`), kebab-case or camelCase consistently in JSON — pick one in the contract and stick to it.
- IDs: opaque path variables (`/tickets/{id}`); never expose internal implementation details in URLs.
- Time: ISO-8601 timestamps in UTC (e.g. `2026-09-22T09:00:00Z`) unless the contract states otherwise.

## HTTP methods

| Method | Use |
|--------|-----|
| `GET` | Read; safe and idempotent |
| `POST` | Create or non-idempotent actions (e.g. add comment) |
| `PUT` / `PATCH` | Update; document which fields are replace vs merge |
| `DELETE` | Remove only if the product requires it |

Status **transitions** must be explicit in the contract (dedicated endpoint or documented field update). Invalid transitions → **4xx**, never silently coerced.

## Status codes

| Code | When |
|------|------|
| `200` | Successful read/update with body |
| `201` | Resource created (include `Location` when practical) |
| `204` | Success with no body (if used) |
| `400` | Validation / malformed request |
| `404` | Resource not found |
| `409` | Conflict (optional: version conflict, duplicate) |
| `422` | Semantically invalid (optional alternative for domain rule failures such as illegal transitions — pick one of `400`/`409`/`422` in the contract and use it consistently) |
| `500` | Unexpected server failure (generic client message) |

Do not return `200` with an embedded error for expected failures.

## Error body shape

All 4xx/5xx JSON errors that the UI must display SHALL use a stable shape:

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

Rules:

- `message`: **human-readable** for UI display.
- `code`: **machine-readable** stable token (`VALIDATION_ERROR`, `NOT_FOUND`, `INVALID_TRANSITION`, …).
- `details`: optional field-level list for forms; omit or empty when not applicable.
- Never include stack traces, SQL, secrets, or raw exception class dumps in `message`/`details`.

Controllers and `@RestControllerAdvice` must produce this shape; the frontend may rely on `message` + `details`.

## Pagination

List endpoints that can grow unbounded MUST support pagination:

| Query param | Meaning | Defaults (suggested) |
|-------------|---------|----------------------|
| `page` | 0-based page index | `0` |
| `size` | Page size | `20` (cap e.g. `100`) |
| `sort` | Optional `field,asc\|desc` | Document default (e.g. `createdAt,desc`) |

Response envelope (suggested):

```json
{
  "content": [ ],
  "page": 0,
  "size": 20,
  "totalElements": 0,
  "totalPages": 0
}
```

If the take-home keeps lists small, a simpler array response is allowed **only if** documented in `spec/api-contract.md`; prefer the envelope for anything production-like.

## Filtering & search

- **Filter by status:** query param, e.g. `?status=OPEN` (exact enum match).
- **Search by keyword:** query param, e.g. `?q=` or `?search=` — document which fields are matched (title, description, etc.).
- Combine with pagination: filters apply before paging.
- Unknown enum values → `400` with `VALIDATION_ERROR` (or equivalent), not empty success pretending the filter worked.
- Empty search string → treat as “no search filter” unless the contract says otherwise.

## Request validation

- Reject unknown required fields missing, blank strings where not allowed, and out-of-range enums at the backend.
- Partial updates: document which fields are optional; omit vs null semantics must be clear in the contract.

## Security & hygiene

- No credentials in URLs, response bodies, or example snippets committed to the repo.
- CORS and auth are product decisions; never “disable security” in shared defaults without an explicit local profile.
- Idempotency/concurrency: if not required, state a simple rule in the contract (e.g. last-write-wins) and stick to it.

## Consistency with specs

When `spec/api-contract.md` and this file disagree, update one of them deliberately — do not leave silent drift. Prefer tightening the contract to match these standards unless a documented exception is required.
