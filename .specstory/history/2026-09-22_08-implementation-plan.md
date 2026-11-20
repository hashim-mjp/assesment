# Prompt 08 — Plan / tasks (no full-app one-shot)

- **Date:** 2026-09-22
- **Tool:** Cursor
- **Source prompt pack:** `prompts/08-implementation-plan.md`
- **Index:** `docs/prompt-history.md`

> Security: do not include secrets, tokens, API keys, passwords, or `.env` contents in prompt history.

## Prompt

```
Using the spec/ files and steering files, produce a sequenced implementation plan and task list.

Do not generate the full application in this step.

Break work into small, reviewable tasks, for example:
1. Project skeletons (Spring Boot + frontend) with no business features
2. Ticket entity + migrations
3. Create/list/get ticket API
4. Update fields + assignee
5. Comments
6. Search and status filter
7. State machine enforcement
8. Backend validation + error mapping
9. UI screens wired to API
10. Tests per spec/test-strategy.md
11. Persistence restart check
12. Secret hygiene check

For each task: files likely touched, spec it implements, and a verification step.

Save the plan as spec/plan.md or a tasks file your IDE already uses. Keep tasks small enough that we implement them one prompt at a time.
```
