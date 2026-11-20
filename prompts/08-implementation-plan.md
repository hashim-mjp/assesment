# Prompt 08 — Plan / tasks (no full-app one-shot)

## Context for the AI

Plan only. The assignment forbids starting with “Build the complete application.”

## Prompt (copy into Cursor)

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

## References

- PDF: *Ask* — workflow Requirement → Specification → Plan / Tasks → Implementation → Testing → Review → Fix.
- PDF: *Ask* — “Do not start by asking AI: Build the complete application.”
- PDF: *Exercise* — main goal is spec-driven development and validating AI output.
- Source file: `Assessments .pdf`.
