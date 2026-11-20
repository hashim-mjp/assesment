# Command: Review spec

Playbook for reviewing specification artefacts before or alongside implementation. Reusable across the project.

## When to run

Before coding a feature, after editing `spec/`, or as Part A of a full review (`commands/review-code.md` is Part B).

## Inputs

- Entire `spec/` tree (create list of missing files if incomplete)
- Application requirements / acceptance criteria for the exercise
- Cross-cutting rules: `rules/api-standards.md`, `rules/testing.md`, `rules/java-springboot.md`
- Documentation sync expectations: `skills/documentation/SKILL.md`

## Expected spec set

```
spec/requirements.md
spec/architecture.md
spec/data-model.md
spec/api-contract.md
spec/state-machine.md
spec/ui-flow.md
spec/test-strategy.md
```

## Process

1. **Completeness** — Requirements covered (create, list, detail, update fields/assignee, comments, search, status filter, persistence, backend validation, meaningful UI errors).
2. **State machine** — Allowed paths and denied examples documented; backend enforcement stated; matches product wording (`OPEN → IN_PROGRESS → RESOLVED → CLOSED`, cancel paths, no reopen from terminal states unless explicitly required).
3. **Internal consistency** — architecture ↔ data-model ↔ api-contract ↔ state-machine ↔ ui-flow ↔ test-strategy agree on names, statuses, fields, and error behaviour.
4. **API standards** — Contract follows `rules/api-standards.md` (status codes, error shape, filter/search/pagination conventions).
5. **Testability** — `test-strategy.md` maps acceptance criteria to unit/slice/integration tests; state-machine suite called out.
6. **Invented scope** — Flag gold-plating (auth systems, notifications, etc.) not required by requirements; recommend removal or explicit deferral.
7. **Secrets** — Specs and examples contain no real credentials.
8. **AI mistakes** — Call out contradictions, impossible transitions, endpoints that conflict, or vague “TBD” where tests need certainty.

## Output format

1. **Findings** by severity (`Critical` / `Major` / `Minor`)
2. **Consistency matrix** (short): which specs agree / conflict
3. **Missing sections** checklist
4. **Fix list** (spec edits only) for the next step
5. Note any requirement that lacks a testable statement

## Do not

- Start implementing application code in this playbook.
- Rewrite the whole spec set when a surgical edit suffices.
- Leave unresolved conflicts between `state-machine.md` and `api-contract.md`.
