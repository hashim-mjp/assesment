# T16 — Secret hygiene check (recorded)

**Date:** 2026-09-22

## Checks

| Check | Result |
|-------|--------|
| `.gitignore` ignores `.env`, `.env.local`, `frontend/.env*`, `backend/data/` | Pass |
| Root `.env.example` uses `change-me` / empty placeholders only | Pass |
| `frontend/.env.example` has only `NEXT_PUBLIC_API_BASE_URL` (no secrets) | Pass |
| YAML passwords are `${SPRING_DATASOURCE_PASSWORD}` or empty (H2 test/local) | Pass |
| No `BEGIN … PRIVATE KEY` / AWS-style keys in source/specs | Pass |
| No real credentials in `backend/src` or committed frontend env | Pass |

## Notes

- Workspace may not be a git repo yet; ignore patterns are still in place for when `git init` / remote is used.
- Do not commit `frontend/.env.local` or filled `.env` files.
