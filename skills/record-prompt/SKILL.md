---
name: record-prompt
description: >-
  Records each user chat prompt to .specstory/history/ and indexes it in
  docs/prompt-history.md. Use on every new assessment prompt when SpecStory
  is not installed, on Kiro, or when the user asks to save/log prompt history.
  Never write secrets, tokens, or .env contents.
---

# Record prompt history

Persist every meaningful user prompt for this assessment. SpecStory can autosave under `.specstory/history/`; when it is **not** in use (or on **Kiro**), follow this skill instead.

## When to run

- Start of a new chat whose prompt drives assessment work
- User asks to save / log / record the prompt
- After copying a prompt from `prompts/*.md` into the AI tool
- SpecStory is unavailable or history file was not created

## Never log

Redact or omit:

- API keys, tokens, passwords, certificates, private keys, OAuth secrets
- `.env` / `.env.*` contents and connection strings with credentials
- Real production URLs with embedded credentials

Replace with placeholders such as `<REDACTED>` or `YOUR_API_KEY`.

## Steps

1. **Capture** the latest user prompt text (the instruction, not the entire agent reply).
2. **Choose a slug** from the topic, e.g. `15-prompt-history`, `t04-create-ticket-api`.
3. **Write** `.specstory/history/YYYY-MM-DD_<slug>.md` using today's date:

```markdown
# <short title>

- **Date:** YYYY-MM-DD
- **Tool:** Cursor | Copilot | Kiro
- **Source prompt pack:** `prompts/NN-name.md` (omit if ad-hoc)
- **Index:** `docs/prompt-history.md`

> Security: do not include secrets, tokens, API keys, passwords, or `.env` contents in prompt history.

## Prompt

```
<prompt text, secrets redacted>
```
```

4. **Append** a row to `docs/prompt-history.md`:
   - Pack prompts → table **Indexed prompts (from `prompts/`)** if not already listed
   - Other chats → table **Ad-hoc / follow-up chats**
   - Columns: Date, Tool (`Cursor` / `Copilot` / `Kiro`), Summary, History link (and Source pack when applicable)

5. **Confirm** in the reply: path of the history file and that the index was updated. Do not dump secrets in the confirmation.

## Example index row

| 2026-09-22 | Cursor | Persist prompt history | `prompts/15-prompt-history.md` | `.specstory/history/2026-09-22_15-prompt-history.md` |

## Related

- Index: `docs/prompt-history.md`
- History dir: `.specstory/history/`
- Documentation skill: `skills/documentation/SKILL.md` (prompt / history hygiene)
