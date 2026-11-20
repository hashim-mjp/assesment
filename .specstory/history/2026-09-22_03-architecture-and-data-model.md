# Prompt 03 — Architecture and data model

- **Date:** 2026-09-22
- **Tool:** Cursor
- **Source prompt pack:** `prompts/03-architecture-and-data-model.md`
- **Index:** `docs/prompt-history.md`

> Security: do not include secrets, tokens, API keys, passwords, or `.env` contents in prompt history.

## Prompt

```
Using spec/requirements.md and the Java Spring Boot / API steering files, create:

- spec/architecture.md
- spec/data-model.md

Do not implement code.

architecture.md must cover:
- Backend modules (API, domain, persistence)
- Frontend app shape (pages/routes for create, list, detail, search/filter)
- How the UI talks to the REST API
- Database choice: PostgreSQL for realistic persistence; H2 acceptable for local/dev if documented, with a path to Postgres
- How data survives application restart
- Where backend validation and error mapping live
- How secrets stay out of git

data-model.md must define entities, fields, types, and relationships at least for:
- Ticket (id, title, description, priority, assignee, status, timestamps, etc.)
- Comment (id, ticket id, body, author if needed, timestamps)
- Any user/assignee representation you choose (keep it simple unless justified)

Include uniqueness, required fields, and indexes useful for keyword search and status filter.

Call out open questions instead of inventing product rules not in the assignment.
```
