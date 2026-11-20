# Prompt 10 — Frontend implementation (incremental)

## Context for the AI

UI after (or in lockstep with) a working API slice. Repeat per screen.

## Prompt (copy into Cursor)

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

## References

- PDF: *Exercise* — React/Next.js or equivalent frontend.
- PDF: *Application Requirements* 1–7, 10.
- PDF: *Core Acceptance Criteria* — create/list/detail/update/assignee/comments/search/filter/errors from UI.
- PDF: *Spec Artefacts* — `ui-flow.md`.
- Source file: `Assessments .pdf`.
