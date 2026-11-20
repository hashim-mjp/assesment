# Prompt 10 — Frontend implementation (incremental)

- **Date:** 2026-09-22
- **Tool:** Cursor
- **Source prompt pack:** `prompts/10-frontend-implementation.md`
- **Index:** `docs/prompt-history.md`

> Security: do not include secrets, tokens, API keys, passwords, or `.env` contents in prompt history.

## Prompt

```
Implement the NEXT unfinished UI task from spec/ui-flow.md and the plan. Use React/Next.js or the equivalent already chosen in architecture.

Do not rebuild the whole frontend at once.

Wire screens to the REST contract:
- Create ticket
- List tickets
- View details
- Update title, description, priority, assignee
- Add comments
- Search by keyword
- Filter by status

UI must display meaningful errors from backend validation and rejected status transitions.

The UI may hide illegal status actions, but must not be the only enforcement.

Match Core Acceptance Criteria that are UI-visible. No secrets in frontend env files committed to git.

After coding, list routes/components and which acceptance criteria they satisfy. Flag any AI-generated UX that contradicts the spec.
```
