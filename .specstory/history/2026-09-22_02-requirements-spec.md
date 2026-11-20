# Prompt 02 — Requirements specification

- **Date:** 2026-09-22
- **Tool:** Cursor
- **Source prompt pack:** `prompts/02-requirements-spec.md`
- **Index:** `docs/prompt-history.md`

> Security: do not include secrets, tokens, API keys, passwords, or `.env` contents in prompt history.

## Prompt

```
Create spec/requirements.md for a Support Ticket Management System.

Do not write implementation code. Capture functional requirements, non-functional constraints, and acceptance criteria so later specs can be derived from this file.

Stack (must appear in the spec as constraints):
- Java 21
- Spring Boot
- PostgreSQL or H2
- REST API
- React/Next.js or equivalent frontend
- Cursor + GitHub Copilot as the build process (not a runtime dependency)

Functional requirements (must all be listed, numbered):
1. Create a ticket.
2. List tickets.
3. View ticket details.
4. Update title, description, priority and assignee.
5. Add comments.
6. Search tickets by keyword.
7. Filter tickets by status.
8. Persist data in a database.
9. Validate input at the backend.
10. Display meaningful errors in the UI.

Also record Core Acceptance Criteria as a checklist that the solution is complete when:
- Ticket can be created from UI
- Tickets can be listed
- Ticket details can be viewed
- Ticket fields can be updated
- Assignee can be changed
- Comments can be added
- Search works
- Status filter works
- Valid status transitions work
- Invalid status transitions are rejected by backend
- Data survives application restart
- Backend validation works
- UI shows meaningful errors
- State-machine integration tests pass
- No secrets are committed

Explicitly note: the application matters, but the assessment is how we analyse requirements, write specs, manage AI context, and validate AI-generated code.

Output only spec/requirements.md unless you need a tiny index pointer in spec/.
```
