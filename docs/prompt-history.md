# Prompt history index

Human-readable index of prompts used for this assessment. Full text lives under [`.specstory/history/`](../.specstory/history/).

## Team process

For **every new chat prompt** (not only the pack under `prompts/`):

1. Save the prompt body to `.specstory/history/YYYY-MM-DD_<slug>.md`  
   - Prefer SpecStory autosave when available.  
   - Otherwise follow [`skills/record-prompt/SKILL.md`](../skills/record-prompt/SKILL.md).
2. Append a row to the table below with:
   - **Date** (`YYYY-MM-DD`)
   - **Tool** (`Cursor` / `Copilot` / `Kiro`)
   - **Summary** (short description)
   - **History link** (path to the saved file)
   - Optional: source pack file under `prompts/` if applicable
3. **Never** log secrets, tokens, API keys, passwords, certificates, or `.env` contents.

## Indexed prompts (from `prompts/`)

| Date | Tool | Summary | Source pack | History |
|------|------|---------|-------------|---------|
| 2026-09-22 | Cursor | Project steering files (generic artefacts) | [`prompts/01-steering-files.md`](../prompts/01-steering-files.md) | [`.specstory/history/2026-09-22_01-steering-files.md`](../.specstory/history/2026-09-22_01-steering-files.md) |
| 2026-09-22 | Cursor | Requirements specification | [`prompts/02-requirements-spec.md`](../prompts/02-requirements-spec.md) | [`.specstory/history/2026-09-22_02-requirements-spec.md`](../.specstory/history/2026-09-22_02-requirements-spec.md) |
| 2026-09-22 | Cursor | Architecture and data model | [`prompts/03-architecture-and-data-model.md`](../prompts/03-architecture-and-data-model.md) | [`.specstory/history/2026-09-22_03-architecture-and-data-model.md`](../.specstory/history/2026-09-22_03-architecture-and-data-model.md) |
| 2026-09-22 | Cursor | REST API contract | [`prompts/04-api-contract.md`](../prompts/04-api-contract.md) | [`.specstory/history/2026-09-22_04-api-contract.md`](../.specstory/history/2026-09-22_04-api-contract.md) |
| 2026-09-22 | Cursor | Ticket status state machine | [`prompts/05-state-machine.md`](../prompts/05-state-machine.md) | [`.specstory/history/2026-09-22_05-state-machine.md`](../.specstory/history/2026-09-22_05-state-machine.md) |
| 2026-09-22 | Cursor | UI flow | [`prompts/06-ui-flow.md`](../prompts/06-ui-flow.md) | [`.specstory/history/2026-09-22_06-ui-flow.md`](../.specstory/history/2026-09-22_06-ui-flow.md) |
| 2026-09-22 | Cursor | Test strategy | [`prompts/07-test-strategy.md`](../prompts/07-test-strategy.md) | [`.specstory/history/2026-09-22_07-test-strategy.md`](../.specstory/history/2026-09-22_07-test-strategy.md) |
| 2026-09-22 | Cursor | Implementation plan / tasks | [`prompts/08-implementation-plan.md`](../prompts/08-implementation-plan.md) | [`.specstory/history/2026-09-22_08-implementation-plan.md`](../.specstory/history/2026-09-22_08-implementation-plan.md) |
| 2026-09-22 | Cursor | Backend implementation (incremental) | [`prompts/09-backend-implementation.md`](../prompts/09-backend-implementation.md) | [`.specstory/history/2026-09-22_09-backend-implementation.md`](../.specstory/history/2026-09-22_09-backend-implementation.md) |
| 2026-09-22 | Cursor | Frontend implementation (incremental) | [`prompts/10-frontend-implementation.md`](../prompts/10-frontend-implementation.md) | [`.specstory/history/2026-09-22_10-frontend-implementation.md`](../.specstory/history/2026-09-22_10-frontend-implementation.md) |
| 2026-09-22 | Cursor | Generate tests | [`prompts/11-generate-tests.md`](../prompts/11-generate-tests.md) | [`.specstory/history/2026-09-22_11-generate-tests.md`](../.specstory/history/2026-09-22_11-generate-tests.md) |
| 2026-09-22 | Cursor | Review spec and code | [`prompts/12-review-spec-and-code.md`](../prompts/12-review-spec-and-code.md) | [`.specstory/history/2026-09-22_12-review-spec-and-code.md`](../.specstory/history/2026-09-22_12-review-spec-and-code.md) |
| 2026-09-22 | Cursor | Fix from review | [`prompts/13-fix-from-review.md`](../prompts/13-fix-from-review.md) | [`.specstory/history/2026-09-22_13-fix-from-review.md`](../.specstory/history/2026-09-22_13-fix-from-review.md) |
| 2026-09-22 | Cursor | Token optimisation and context hygiene | [`prompts/14-token-optimisation.md`](../prompts/14-token-optimisation.md) | [`.specstory/history/2026-09-22_14-token-optimisation.md`](../.specstory/history/2026-09-22_14-token-optimisation.md) |
| 2026-09-22 | Cursor | Persist prompt history | [`prompts/15-prompt-history.md`](../prompts/15-prompt-history.md) | [`.specstory/history/2026-09-22_15-prompt-history.md`](../.specstory/history/2026-09-22_15-prompt-history.md) |

## Ad-hoc / follow-up chats

| Date | Tool | Summary | History |
|------|------|---------|---------|
| _YYYY-MM-DD_ | _Cursor / Copilot / Kiro_ | _Short description_ | _`.specstory/history/...`_ |

<!-- Append new rows above this comment. Do not paste secrets. -->
