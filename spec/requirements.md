# Support Ticket Management System — Requirements

## Purpose and assessment focus

This document captures product requirements for a Support Ticket Management System. It is the source of truth for later specification artefacts (`architecture.md`, `data-model.md`, `api-contract.md`, `state-machine.md`, `ui-flow.md`, `test-strategy.md`) and for implementation and review.

**Assessment note:** A working application matters, but the assessment evaluates how we analyse requirements, write specifications, manage AI context, and validate AI-generated code. Specs and process come before “build the complete application.”

This file does **not** prescribe implementation details, endpoint shapes, or UI wireframes beyond what is needed to derive those later specs.

---

## Stack constraints

These are mandatory technology and process constraints (not optional preferences):

| Constraint | Requirement |
|------------|-------------|
| Language / runtime | **Java 21** |
| Backend framework | **Spring Boot** |
| Persistence | **PostgreSQL** or **H2** (H2 acceptable for local/dev if documented; production-realistic path is PostgreSQL) |
| API style | **REST API** over HTTP/JSON |
| Frontend | **React / Next.js** or an equivalent modern SPA/SSR frontend |
| Build process | **Cursor** and **GitHub Copilot** are used to author and refine the solution; they are **not** runtime dependencies |

---

## Functional requirements

1. **Create a ticket.** A user can create a support ticket with the required fields (at minimum title and description; priority and assignee may be set on create or updated later per later specs).
2. **List tickets.** A user can view a list of existing tickets.
3. **View ticket details.** A user can open a single ticket and see its full details (including status, priority, assignee, description, and related comments as defined in later specs).
4. **Update title, description, priority and assignee.** A user can change a ticket’s title, description, priority, and assignee after creation.
5. **Add comments.** A user can add comments to a ticket; comments are associated with that ticket and visible in ticket details.
6. **Search tickets by keyword.** A user can search tickets using a keyword (matching relevant text fields such as title and/or description — exact fields to be defined in data model / API specs).
7. **Filter tickets by status.** A user can filter the ticket list by status.
8. **Persist data in a database.** Ticket and comment data are stored in a database so they are not lost when the process stops.
9. **Validate input at the backend.** The backend validates requests (required fields, formats, domain rules). Invalid input is rejected; UI-only validation is not sufficient.
10. **Display meaningful errors in the UI.** When the backend rejects a request or an operation fails, the UI shows clear, actionable error messages (not opaque failures or silent no-ops).

### Status lifecycle (domain rule)

Tickets have a **status** that follows a backend-enforced state machine. Valid transitions must succeed; invalid transitions must be rejected by the backend (not only blocked in the UI). The definitive transition table lives in `spec/state-machine.md` (derived from this requirement and the assignment’s allowed/illegal examples). Acceptance of the solution depends on both valid and invalid transition behaviour (see Core Acceptance Criteria).

---

## Non-functional constraints

| Area | Constraint |
|------|------------|
| Persistence durability | Data must survive application restart when using a durable store configuration (PostgreSQL, or H2 file-based / equivalent — document the chosen mode in architecture). |
| Validation location | Input and domain validation (including status transitions) are enforced on the **backend**. |
| Error surfacing | API errors must be mappable to meaningful UI messages (stable error shape to be defined in API standards / contract). |
| Secrets | No secrets (API keys, passwords, connection credentials with real values) may be committed to the repository. Use env vars / local config ignored by git. |
| Testing | State-machine behaviour must be covered by **integration tests** that pass as part of done. |
| Spec-driven delivery | Behaviour is specified before (or alongside) code; later specs and tests derive from this file. |
| Security defaults | No hardcoded credentials; no intentional auth/validation bypasses for convenience. |

---

## Core Acceptance Criteria

The solution is **complete** when all of the following are true:

- [ ] Ticket can be created from UI
- [ ] Tickets can be listed
- [ ] Ticket details can be viewed
- [ ] Ticket fields can be updated
- [ ] Assignee can be changed
- [ ] Comments can be added
- [ ] Search works
- [ ] Status filter works
- [ ] Valid status transitions work
- [ ] Invalid status transitions are rejected by backend
- [ ] Data survives application restart
- [ ] Backend validation works
- [ ] UI shows meaningful errors
- [ ] State-machine integration tests pass
- [ ] No secrets are committed

---

## Out of scope for this document (later specs)

The following are intentionally deferred so this file stays a requirements baseline:

| Concern | Derive in |
|---------|-----------|
| Modules, layers, deployment shape | `spec/architecture.md` |
| Entities, fields, indexes | `spec/data-model.md` |
| Endpoints, payloads, HTTP codes | `spec/api-contract.md` |
| Full status transition table | `spec/state-machine.md` |
| Screens and navigation | `spec/ui-flow.md` |
| Test types and coverage map | `spec/test-strategy.md` |

---

## Traceability

| Requirement | Typical later coverage |
|-------------|------------------------|
| FR1–FR5, FR8 | Architecture, data model, API, UI flow |
| FR6–FR7 | API (query params), UI (search/filter), indexes |
| FR9 | API error contract, backend validation |
| FR10 | UI flow, API error mapping |
| Status lifecycle | State machine, integration tests |
| Acceptance checklist | Test strategy + manual/UI verification |
