---
description: Unit, slice, and integration testing standards; state-machine tests required
globs: "**/*.{java,ts,tsx,js,jsx}"
alwaysApply: false
---

# Testing Guidelines

Reusable testing standards for this repository. Specs under `spec/` (especially `test-strategy.md` and `state-machine.md`) define *what* to prove; this file defines *how*.

## Principles

- Tests are **spec-driven**: assert behaviour from `spec/`, not implementation trivia.
- Prefer a few high-signal tests over snapshot noise or brittle UI pixel checks.
- Do not claim green builds without running the suite.
- If a generated test contradicts the spec, **the spec wins** — fix the test (or flag a real spec bug).
- **No secrets** in fixtures, dumps, or CI logs.

## Test pyramid

| Type | Scope | Typical tools | Use for |
|------|--------|---------------|---------|
| **Unit** | Domain/service logic in isolation | JUnit 5, AssertJ, Mockito (sparingly) | Validation rules, state-transition allow/deny, pure mappers |
| **Slice** | One Spring layer / web layer | `@WebMvcTest`, `@DataJpaTest`, `@JsonTest` | REST contract, error body shape, repository queries |
| **Integration** | App + real-ish infrastructure | `@SpringBootTest`, MockMvc/TestRestTemplate, H2 or Testcontainers Postgres | Persistence across context/DB session, end-to-end API flows, **state machine** |

Frontend tests (React/Next): only where they add confidence (form errors, list/filter behaviour). Do not over-test.

## State-machine tests (required)

When `spec/state-machine.md` exists, maintain an explicit **state-machine integration suite** that proves:

1. Every **allowed** transition succeeds (correct resulting status, HTTP success).
2. Every **denied** transition is **rejected by the backend** (4xx), including examples such as `CLOSED→OPEN`, `RESOLVED→OPEN`, `CANCELLED→OPEN`.
3. Rejection is not UI-only — call the API (or service boundary used by the API) directly in tests.

Map each transition from the spec to at least one test case. Name tests after the transition (e.g. `rejectsClosedToOpen`).

## What each layer should cover

### Unit

- Input validation helpers and domain invariants
- Transition matrix / policy functions
- Edge cases: empty strings, oversized fields, null IDs

### Slice / API

- Status codes and response shapes per `rules/api-standards.md` and `spec/api-contract.md`
- Validation failures → machine-readable + human-readable error body
- Search by keyword and filter by status query params

### Integration

- Create → read after new application context or new DB session (persistence / restart confidence)
- Comments attached to tickets
- Full status lifecycle happy path + illegal transitions
- Optional: Flyway/Liquibase migrations applied cleanly

## Structure & naming

- Mirror production packages under `src/test/java` (or project equivalent).
- Use given/when/then or Arrange-Act-Assert; one logical behaviour per test.
- Share builders/fixtures; avoid copying large JSON blobs without reason.

## CI expectations

- Default test command must run unit + slice + integration suites needed for acceptance.
- Flaky tests are defects: fix or quarantine with an explicit ticket — do not ignore.
- Coverage is a guide, not a goal; **acceptance criteria** and the state-machine suite matter more than a percentage.

## Anti-patterns

- Testing only the happy path for status changes
- Mocking away the transition policy so “integration” tests cannot fail
- Asserting on internal private methods
- Committing credentials or production-like secrets “for realism”
- Blindly accepting AI-generated tests that invent endpoints or statuses not in the spec

## Related playbooks

- Generate or extend suites: `commands/generate-tests.md`
- After implementation: `commands/review-code.md` (missing denied-transition tests is a high-severity finding)
