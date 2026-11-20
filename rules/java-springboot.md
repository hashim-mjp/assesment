---
description: Java 21 + Spring Boot backend conventions (layers, validation, exceptions, secrets)
globs: "**/*.{java,xml,yml,yaml,properties,gradle,kts}"
alwaysApply: false
---

# Java Spring Boot Guidelines

Reusable standards for all backend work in this repository. Prefer these over ad-hoc chat advice.

## Stack

- **Language:** Java 21 (use modern APIs: records, sealed types, pattern matching, virtual threads where appropriate)
- **Framework:** Spring Boot (current stable for the project)
- **Persistence:** Spring Data JPA + PostgreSQL in production-like runs; H2 or Testcontainers acceptable for tests
- **API:** Spring Web (REST controllers), Bean Validation (`jakarta.validation`)

## Layered architecture

Keep responsibilities separated. Do not put business rules in controllers or persistence entities unless the project explicitly chooses a thin-entity style and documents it.

| Layer | Responsibility | Must not |
|-------|----------------|----------|
| Controller | HTTP mapping, status codes, DTO in/out | Business rules, direct repository calls |
| Service / application | Use cases, orchestration, transactions | HTTP types (`HttpServlet*`, raw `ResponseEntity` logic beyond mapping) |
| Domain | Invariants, state transitions, pure rules | Framework annotations except where unavoidable |
| Repository / persistence | Data access | Validation of business rules, status-machine policy |
| DTO / API models | Request/response shapes | Persistence annotations |

Dependency direction: **Controller → Service → Domain/Repository**. Never reverse.

## Packages (suggested)

```
…/api          # controllers, request/response DTOs, exception advice
…/application  # services / use cases
…/domain       # entities or aggregates, enums, state machine
…/persistence  # repositories, JPA mappings if split from domain
…/config       # non-secret configuration beans
```

Adapt names to the chosen module layout; keep the layer boundaries clear.

## Validation

- Validate **all write inputs** at the API boundary with Bean Validation (`@Valid`, `@NotBlank`, `@Size`, `@Email`, etc.).
- Enforce **domain invariants** in domain/service code (e.g. status transitions), not only in the UI.
- Prefer fail-fast: invalid input → `400` with the standard error body (see `rules/api-standards.md`).
- Never trust the client for authorization, status transitions, or “current state”.

## Exceptions

- Use a small set of domain/application exceptions (e.g. not found, conflict, invalid transition, validation).
- Map them in `@RestControllerAdvice` to HTTP status + the shared error body shape.
- Do not leak stack traces, SQL, or internal paths to clients.
- Log unexpected errors with correlation-safe messages; do not log secrets or full PII dumps.

## Transactions & persistence

- Put `@Transactional` on application/service methods that change state; keep controllers non-transactional.
- Prefer explicit entity updates over silent partial merges unless the API contract defines patch semantics.
- Ensure data **survives restart** (real DB, not in-memory-only for demo acceptance unless H2 file mode is intentional and documented).

## Configuration & secrets

- **Never** hardcode passwords, API keys, tokens, or connection credentials in source, tests, or docs.
- Use environment variables or external config; commit only `.env.example` / sample YAML **without values**.
- Keep secrets out of logs, exception messages, and git history.
- Disable permissive CORS and auth bypasses in production profiles; document any local-only relaxations.

## Code style

- Prefer immutable DTOs (records) for API payloads.
- Name types after the domain (Ticket, Comment, TicketStatus), not CRUD jargon alone.
- Avoid `System.out`; use SLF4J.
- No `eval`-like patterns, unsafe deserialization of untrusted payloads, or disabled TLS for convenience.

## Out of scope for this rule alone

Frontend, OpenAPI generation details, and test mechanics live in `rules/api-standards.md` and `rules/testing.md`. Specs under `spec/` are the source of truth for product behaviour.
