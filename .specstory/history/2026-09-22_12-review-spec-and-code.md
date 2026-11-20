# Prompt 12 — Review spec and code

- **Date:** 2026-09-22
- **Tool:** Cursor
- **Source prompt pack:** `prompts/12-review-spec-and-code.md`
- **Index:** `docs/prompt-history.md`

> Security: do not include secrets, tokens, API keys, passwords, or `.env` contents in prompt history.

## Prompt

```
Run a two-part review using commands/review-spec.md and commands/review-code.md.

Part A — Spec review:
- Are spec files complete vs Application Requirements 1–10 and the state machine?
- Do architecture, data-model, api-contract, state-machine, ui-flow, and test-strategy agree?
- Any invented requirements that should be removed?

Part B — Code review:
- Spec-driven? Dead code? Missing validation?
- State machine enforced only in UI?
- Invalid transitions accepted?
- Errors not useful for the UI?
- Persistence / restart risk?
- Secrets committed?
- Tests missing for denied transitions?

Output:
1. Findings ordered by severity
2. At least a few meaningful mistakes or incorrect suggestions (from AI or from the current code/spec) — this is required for the assessment
3. A short fix list for the next prompt
4. Traceability: requirement → spec → code → test

Do not implement fixes in this step unless a finding is a one-line typo you explicitly call out.
```
