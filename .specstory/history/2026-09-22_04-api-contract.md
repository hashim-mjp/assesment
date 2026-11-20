# Prompt 04 — REST API contract

- **Date:** 2026-09-22
- **Tool:** Cursor
- **Source prompt pack:** `prompts/04-api-contract.md`
- **Index:** `docs/prompt-history.md`

> Security: do not include secrets, tokens, API keys, passwords, or `.env` contents in prompt history.

## Prompt

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
