# Prompt 05 — Ticket status state machine

- **Date:** 2026-09-22
- **Tool:** Cursor
- **Source prompt pack:** `prompts/05-state-machine.md`
- **Index:** `docs/prompt-history.md`

> Security: do not include secrets, tokens, API keys, passwords, or `.env` contents in prompt history.

## Prompt

```
Write spec/state-machine.md that the backend MUST enforce.

Allowed transitions:
- OPEN → IN_PROGRESS → RESOLVED → CLOSED
- OPEN → CANCELLED
- IN_PROGRESS → CANCELLED

Illegal examples that must be rejected:
- CLOSED → OPEN
- RESOLVED → OPEN
- CANCELLED → OPEN

Also specify:
- Initial status on create (OPEN unless you justify otherwise from requirements — default to OPEN).
- Whether RESOLVED → CLOSED is the only way to close after resolve.
- Whether CANCELLED is terminal (no further transitions).
- Whether CLOSED is terminal.
- What HTTP status and error code the API returns on invalid transition.
- That enforcement lives in the backend (domain service), not only in the UI.

Include a transition table: from-status × to-status → allow/deny.

Do not implement code in this step. Later tests must be able to copy this table.
```
