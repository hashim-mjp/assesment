# Prompt 01 — Project steering files (generic artefacts)

## Context for the AI

You are setting up reusable AI instructions for a Java 21 + Spring Boot + React/Next.js Support Ticket Management System. Do **not** implement application features yet. Create steering files only.

## Prompt (copy into Cursor)

```
Set up spec-driven development hygiene for this repo. Do not generate application code yet.

Create reusable AI steering files that the rest of the project will follow:

1. Java Spring Boot guidelines (Java 21, Spring Boot, layered architecture, validation, exceptions, security of secrets).
2. Testing guidelines (unit, slice, integration; state-machine tests required later).
3. API standards (REST, status codes, error body shape, pagination/filtering conventions).
4. Documentation skill (how to keep specs in sync with code).
5. Commands / playbooks:
   - review code
   - review spec
   - generate tests

Use this structure (adapt only if the IDE requires it; keep the intent):

rules/
  java-springboot.md
  testing.md
  api-standards.md
skills/
  documentation/
commands/
  review-code.md
  review-spec.md
  generate-tests.md

Constraints:
- Instructions must be reusable across the project context, not one-off chat advice.
- No secrets, no hardcoded credentials.
- Do not start implementing the ticket system in this step.

After writing the files, list each path and a one-line purpose.
```

## References

- PDF: *Generic Artefacts* — Java Springboot Guidelines, Testing guidelines, API Standards, Documentation Skills, Commands to review code/spec and generate tests.
- PDF: example Cursor layout (`rules/`, `skills/documentation/`, `commands/`).
- PDF: *Ask* workflow — start with Requirement/Specification hygiene, not “Build the complete application.”
- Source file: `Assessments .pdf` (workspace root).
