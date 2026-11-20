# Prompt 05 — Ticket status state machine

## Context for the AI

The backend must enforce this machine. Invalid transitions are a hard reject.

## Prompt (copy into Cursor)

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

## References

- PDF: *Application Requirements* — “The following state machine must be enforced by the backend” and illegal examples.
- PDF: *Spec Artefacts* — `state-machine.md`.
- PDF: *Core Acceptance Criteria* — valid transitions work; invalid rejected by backend; state-machine integration tests pass.
- Source file: `Assessments .pdf`.
