# Prompt 09 — Backend implementation (incremental)

- **Date:** 2026-09-22
- **Tool:** Cursor
- **Source prompt pack:** `prompts/09-backend-implementation.md`
- **Index:** `docs/prompt-history.md`

> Security: do not include secrets, tokens, API keys, passwords, or `.env` contents in prompt history.

## Prompt

```
Implement the NEXT unfinished backend task from the plan (read spec/plan.md and the existing code). Do not implement the entire system in one pass.

Follow:
- rules/java-springboot.md
- spec/api-contract.md
- spec/data-model.md
- spec/state-machine.md
- spec/requirements.md

Hard rules:
- Java 21 + Spring Boot
- Persist with PostgreSQL or H2 as specified in architecture
- Validate input on the backend
- Enforce the ticket state machine on the backend; reject invalid transitions
- Meaningful error responses for the UI
- No secrets in source

When the current task is status-related, implement the full allowed set:
OPEN → IN_PROGRESS → RESOLVED → CLOSED
OPEN → CANCELLED
IN_PROGRESS → CANCELLED
and reject CLOSED→OPEN, RESOLVED→OPEN, CANCELLED→OPEN (and any other illegal pair from the spec table).

After coding:
- List files changed
- Map them to requirement numbers
- Note anything in the spec that looks wrong or incomplete (do not silently invent product rules)

Stop when this one task is done and buildable.
```
