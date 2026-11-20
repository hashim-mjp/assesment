---
name: documentation
description: >-
  Keeps specifications synchronized with implementation and tests. Use when
  editing spec/, implementing features, changing APIs or state machines,
  reviewing drift between docs and code, or when the user asks to update
  documentation, architecture notes, or API contracts.
---

# Documentation Skill

Maintain **spec-driven development** hygiene: specifications are the source of truth for behaviour; code and tests must follow them, and docs must be updated when behaviour intentionally changes.

## Spec layout (expected)

```
spec/
├── requirements.md
├── architecture.md
├── data-model.md
├── api-contract.md
├── state-machine.md
├── ui-flow.md
└── test-strategy.md
```

Create missing files only when the workflow asks for that artefact — do not invent product requirements.

## When to update specs

| Change | Update |
|--------|--------|
| New/changed product rule | `requirements.md` (+ related specs) |
| Layering, modules, tech choices | `architecture.md` |
| Entities, fields, relationships | `data-model.md` |
| Endpoints, status codes, payloads | `api-contract.md` (+ `rules/api-standards.md` if cross-cutting) |
| Allowed/denied status transitions | `state-machine.md` (+ tests per `rules/testing.md`) |
| Screens, navigation, error display | `ui-flow.md` |
| What/how we prove acceptance | `test-strategy.md` |

## Sync workflow

1. **Read** the relevant `spec/` files before coding.
2. **Implement** against the contract; do not silently extend the product.
3. If implementation discovers a needed change:
   - Update the **spec first** (or in the same change set).
   - Then update code and tests.
   - Call out the delta in the PR/summary.
4. **Never** leave code behaviour that contradicts `state-machine.md` or `api-contract.md`.
5. After API or state changes, ensure `commands/generate-tests.md` coverage still matches the spec.

## Drift checklist

Before marking work done:

- [ ] Requirements 1–10 (or current requirement set) still map to specs
- [ ] Architecture diagram/text matches packages and data store choice
- [ ] Data model matches entities/migrations
- [ ] API contract matches controllers and error shape
- [ ] State machine matches backend enforcement (not UI-only)
- [ ] UI flow matches screens and error handling
- [ ] Test strategy maps acceptance criteria → test types/locations

## Writing style for specs

- Precise, testable statements (“Invalid transition returns 4xx with code `INVALID_TRANSITION`”).
- Prefer tables for transitions, endpoints, and field lists.
- No secrets, credentials, or real connection strings — use placeholders only.
- Avoid duplicating large code blocks; link to rules (`rules/*.md`) for cross-cutting standards.

## Prompt / history hygiene

Record meaningful prompts in `.specstory/history/` and index them in `docs/prompt-history.md`. Prefer SpecStory autosave; otherwise follow `skills/record-prompt/SKILL.md`. Do not paste secrets, tokens, or `.env` contents into history files.

## Related commands

- Spec completeness and consistency: `commands/review-spec.md`
- Code vs spec: `commands/review-code.md`
