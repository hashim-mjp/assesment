# Command: Review code

Playbook for reviewing implementation against specs and project rules. Reusable across features — do not treat as one-off chat advice.

## When to run

After implementation or when asked to review a PR/diff. Pair with `commands/review-spec.md` when specs may also be wrong.

## Inputs

- Diff or changed files
- `spec/` (at least `api-contract.md`, `state-machine.md`, `requirements.md`, `test-strategy.md`)
- `rules/java-springboot.md`, `rules/api-standards.md`, `rules/testing.md`

## Process

1. **Traceability** — For each touched behaviour, confirm path: requirement → spec → code → test.
2. **Spec alignment** — Controllers/services match `api-contract.md`; transitions match `state-machine.md`.
3. **Architecture** — Layering respected (no business rules trapped only in UI or only in controllers).
4. **Validation & errors** — Backend validates writes; error bodies match API standards (human + machine readable).
5. **State machine** — Allowed transitions work; **denied transitions rejected by backend** (not UI-only). Flag if tests omit denied cases.
6. **Persistence** — Data not solely in memory when persistence is required; restart/session survival considered.
7. **Security** — No secrets, hardcoded credentials, permissive production security shortcuts, or leaked internals in responses.
8. **Tests** — Suites exist for validation, search/filter, comments, persistence, and state-machine allow/deny matrix per `rules/testing.md`.
9. **Dead / invented code** — Remove or flag unused paths and AI-invented endpoints/fields not in the spec.
10. **AI mistakes** — Explicitly hunt for incorrect suggestions (wrong transitions, wrong status codes, tests that cannot fail, etc.).

## Output format

1. **Findings** ordered by severity (`Critical` / `Major` / `Minor` / `Nit`)
2. At least a few **meaningful mistakes or incorrect AI suggestions** when present (required for assessment honesty — do not rubber-stamp)
3. Short **fix list** for the next change set
4. **Traceability** summary: requirement → spec → code → test (table or bullets)

## Severity guide

| Severity | Examples |
|----------|----------|
| Critical | Secrets committed; invalid transitions accepted; no backend validation; data lost on restart |
| Major | Error body unusable by UI; missing state-machine tests; contract/code mismatch |
| Minor | Naming, missing edge-case test, weak log messages |
| Nit | Style-only |

## Do not

- Implement large fixes in the review step unless asked (one-line typo OK if called out).
- Expand scope beyond the reviewed change without noting it.
- Accept generated tests that contradict the spec.
