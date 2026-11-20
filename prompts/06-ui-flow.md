# Prompt 06 — UI flow

## Context for the AI

Map screens to acceptance criteria. No feature implementation yet.

## Prompt (copy into Cursor)

```
Write spec/ui-flow.md for the Support Ticket Management System frontend (React/Next.js or equivalent).

No application code.

Describe screens, navigation, and empty/error/success states for:
- Create ticket from UI
- List tickets
- View ticket details
- Update title, description, priority, assignee
- Add comments
- Search by keyword
- Filter by status
- Display meaningful errors from backend validation and invalid status transitions

For each flow include: user action → API call (from api-contract) → UI result.

Call out that the UI must not be the only guard for status transitions; it may hide illegal actions but the backend still rejects them.

Keep the UX simple: one list view, one detail view, create/edit forms, comment thread.
```

## References

- PDF: *Spec Artefacts* — `ui-flow.md`.
- PDF: *Core Acceptance Criteria* — create/list/detail/update/assignee/comments/search/filter/errors from UI.
- PDF: *Application Requirements* item 10 — meaningful errors in the UI.
- PDF: *Exercise* — React/Next.js or equivalent.
- Source file: `Assessments .pdf`.
