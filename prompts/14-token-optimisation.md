# Prompt 14 — Token optimisation and context hygiene

## Context for the AI

Keep the AI working from specs and steering files instead of dumping the whole repo every time.

## Prompt (copy into Cursor)

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

## References

- PDF: *Token Optimisation* — Graphify, Caveman, Codebase-memory MCP.
- PDF: *Exercise* — manage AI context; spec-driven development with Cursor / Copilot / Kiro.
- Source file: `Assessments .pdf`.
