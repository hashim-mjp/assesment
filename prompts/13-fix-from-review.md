# Prompt 13 — Fix from review

## Context for the AI

Last workflow step after Review. Keep the change set small.

## Prompt (copy into Cursor)

```
Implement only the agreed fix list from the last review.

Constraints:
- Smallest change that restores spec compliance
- Re-run the relevant tests, especially state-machine integration tests
- Do not refactor unrelated modules
- Do not weaken validation or allow illegal transitions to make tests pass
- If a review finding disagrees with spec/state-machine.md, ask — do not “fix” the spec away unless the spec itself is wrong vs the assignment PDF

After fixes, summarise: what changed, why, which files, which tests were run.
```

## References

- PDF: *Ask* workflow — Fix.
- PDF: *Important* — do not blindly accept AI output; validate and correct.
- PDF: *Core Acceptance Criteria* — invalid transitions rejected; tests pass; no secrets.
- Source file: `Assessments .pdf`.
