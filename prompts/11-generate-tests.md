# Prompt 11 — Generate tests

## Context for the AI

Use the generate-tests command style from steering files. Prefer spec-driven tests over snapshot noise.

## Prompt (copy into Cursor)

```
Follow commands/generate-tests.md and spec/test-strategy.md.

Generate (or complete) tests for the Support Ticket system. Highest priority:

1. State-machine integration tests — every allowed transition succeeds; every denied transition is rejected by the backend (including CLOSED→OPEN, RESOLVED→OPEN, CANCELLED→OPEN).
2. Backend validation tests — invalid create/update payloads.
3. Persistence — create ticket, restart or new application context / new DB session, data still present.
4. Search by keyword and filter by status.
5. Comments on a ticket.

Do not claim tests pass without running them. Run the test command and fix failures.

If generated tests contradict spec/state-machine.md or spec/api-contract.md, treat the spec as source of truth and report the mistake.

No secrets in test fixtures.
```

## References

- PDF: *Generic Artefacts* — Testing guidelines; `commands/generate-tests.md`.
- PDF: *Ask* workflow — Testing.
- PDF: *Core Acceptance Criteria* — state-machine integration tests; persistence; validation; search; filter.
- PDF: *Important* — catch incorrect AI test suggestions.
- Source file: `Assessments .pdf`.
