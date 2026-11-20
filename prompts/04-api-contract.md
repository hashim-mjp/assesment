# Prompt 04 — REST API contract

## Context for the AI

Depends on `spec/requirements.md`, `spec/architecture.md`, `spec/data-model.md`, and `rules/api-standards.md` if present.

## Prompt (copy into Cursor)

```
Write spec/api-contract.md for the Support Ticket REST API.

No implementation.

Cover request/response shapes, HTTP methods, paths, status codes, and error bodies for:
- Create ticket
- List tickets
- Get ticket details
- Update title, description, priority, assignee
- Add comment
- Search by keyword
- Filter by status
- Status transition (if separate from update; if combined, specify exactly how status is changed)

Rules:
- Follow the project's API standards steering file.
- Backend validation failures must return a machine-readable + human-readable error the UI can display.
- Invalid status transitions must be rejected (4xx), not silently coerced.
- Document query parameters for search and status filter.
- Document idempotency/concurrency only if you choose a simple rule (e.g. last-write-wins) and state it.

Keep the contract small enough to implement in a take-home, complete enough that frontend and tests can be written against it.
```

## References

- PDF: *Spec Artefacts* — `api-contract.md`.
- PDF: *Generic Artefacts* — API Standards.
- PDF: *Application Requirements* 1–10.
- PDF: *Core Acceptance Criteria* — backend validation; UI meaningful errors; invalid transitions rejected by backend.
- Source file: `Assessments .pdf`.
