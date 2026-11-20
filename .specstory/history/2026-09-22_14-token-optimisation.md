# Prompt 14 — Token optimisation and context hygiene

- **Date:** 2026-09-22
- **Tool:** Cursor
- **Source prompt pack:** `prompts/14-token-optimisation.md`
- **Index:** `docs/prompt-history.md`

> Security: do not include secrets, tokens, API keys, passwords, or `.env` contents in prompt history.

## Prompt

```
Propose a token-efficient way to work in this repo without changing product behaviour.

Prefer:
- Pointing the AI at spec/*.md and rules/*.md instead of pasting large files
- Small task prompts (one slice per chat)
- Plugins / MCP called out in the assignment if available: Graphify, Caveman, Codebase-memory MCP — only if they are already usable; do not invent a fake integration
- A short “context pack” list: which files to @-mention for backend vs frontend vs tests

Deliverable: a short docs/context-strategy.md (or a section in README) listing which files to attach per task type.

Do not rewrite the application.
```
