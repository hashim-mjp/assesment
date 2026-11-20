# Command: Generate tests

Playbook for creating or completing tests from specifications. Spec-driven; no snapshot noise. Reusable for any feature that has specs.

## When to run

After `spec/test-strategy.md` (and related specs) exist, and enough code is present to compile/run tests — or to scaffold tests that compile against the contract.

## Inputs

- `spec/test-strategy.md` (primary plan)
- `spec/state-machine.md`, `spec/api-contract.md`, `spec/requirements.md`
- `rules/testing.md`, `rules/api-standards.md`
- Existing test source tree and build commands

## Priority order

Generate or complete in this order unless the user narrows scope:

1. **State-machine integration tests** — every allowed transition succeeds; every denied transition is rejected by the **backend** (include `CLOSED→OPEN`, `RESOLVED→OPEN`, `CANCELLED→OPEN` and all other denies in the matrix).
2. **Backend validation** — invalid create/update payloads → correct status + error body shape.
3. **Persistence** — create entity; new application context / DB session; data still present.
4. **Search by keyword** and **filter by status**.
5. **Comments** (or other subordinate resources) on the parent entity.
6. Remaining items from `test-strategy.md` / acceptance criteria.

## Process

1. Read specs; list behaviours still untested.
2. Choose unit vs slice vs integration per `rules/testing.md` and `test-strategy.md`.
3. Write tests that would **fail** if the behaviour were wrong (no tautologies, no over-mocking the policy under test).
4. Use the project’s test stack only (do not add dependencies unless necessary and justified).
5. **Run** the test command; fix failures or fix product code if the test correctly encodes the spec.
6. If a generated test contradicts the spec, treat the **spec as source of truth**, fix the test, and report the mistake.
7. Ensure fixtures contain **no secrets**.

## Output format

1. Files added/updated
2. Mapping: acceptance criterion / transition → test class/method
3. Commands run and results (pass/fail counts)
4. Gaps still open (if any) and suggested next tests
5. Any AI mistakes caught (wrong endpoint, wrong status, invented transition)

## Do not

- Claim tests pass without running them.
- Invent endpoints, statuses, or fields absent from the contract.
- Commit credentials or production connection strings in test config.
- Replace the state-machine suite with UI-only tests.
