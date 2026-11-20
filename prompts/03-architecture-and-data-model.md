# Prompt 03 — Architecture and data model

## Context for the AI

Depends on `spec/requirements.md`. Produce architecture and persistence model only.

## Prompt (copy into Cursor)

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

## References

- PDF: *Spec Artefacts* — `architecture.md`, `data-model.md`.
- PDF: *Exercise* — Java 21, Spring Boot, PostgreSQL/H2, REST, React/Next.js.
- PDF: *Application Requirements* — persist data; validate backend.
- PDF: *Core Acceptance Criteria* — data survives restart; no secrets committed.
- Source file: `Assessments .pdf`.
