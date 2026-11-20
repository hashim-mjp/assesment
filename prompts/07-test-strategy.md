# Prompt 07 — Test strategy

## Context for the AI

Testing is a first-class assessment goal. State-machine integration tests are required.

## Prompt (copy into Cursor)

```
Write spec/test-strategy.md aligned with the testing guidelines steering file.

No production code.

Cover:
- Unit tests for domain validation and state transitions
- API/slice tests for REST contract and error bodies
- Integration tests with the real database (H2 or Testcontainers Postgres) proving data survives restart / is persisted
- Frontend tests only where they add confidence (form errors, list/filter); do not over-test
- Explicit suite: state-machine integration tests for every allowed and denied transition in spec/state-machine.md
- How we will prove invalid transitions are rejected by the backend
- How we will prove search and status filter
- How we will prove no secrets are committed (e.g. .gitignore, env examples without values)

Map each Core Acceptance Criterion to a test type and location.

Include what “done” looks like before Review.
```

## References

- PDF: *Spec Artefacts* — `test-strategy.md`.
- PDF: *Generic Artefacts* — Testing guidelines; command `generate-tests`.
- PDF: *Ask* workflow — Testing before Review.
- PDF: *Core Acceptance Criteria* — state-machine integration tests pass; persistence; validation.
- PDF: *Important* — identify AI mistakes; do not blindly accept generated tests.
- Source file: `Assessments .pdf`.
