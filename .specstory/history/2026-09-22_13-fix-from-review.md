# Prompt 13 — Fix from review

- **Date:** 2026-09-22
- **Tool:** Cursor
- **Source prompt pack:** `prompts/13-fix-from-review.md`
- **Index:** `docs/prompt-history.md`

> Security: do not include secrets, tokens, API keys, passwords, or `.env` contents in prompt history.

## Prompt

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
