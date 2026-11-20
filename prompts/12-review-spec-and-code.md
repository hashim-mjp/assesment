# Prompt 12 — Review spec and code

## Context for the AI

Review is a required workflow step. You must look for AI mistakes, not only style nits.

## Prompt (copy into Cursor)

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

## References

- PDF: *Ask* workflow — Review.
- PDF: *Generic Artefacts* — `review-code.md`, `review-spec.md`.
- PDF: *Important* — “You must identify at least meaningful mistakes or incorrect suggestions by the AI.”
- PDF: *Exercise* — how you manage AI context and validate AI-generated code.
- Source file: `Assessments .pdf`.
