# Prompt 11 — Generate tests

- **Date:** 2026-09-22
- **Tool:** Cursor
- **Source prompt pack:** `prompts/11-generate-tests.md`
- **Index:** `docs/prompt-history.md`

> Security: do not include secrets, tokens, API keys, passwords, or `.env` contents in prompt history.

## Prompt

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
