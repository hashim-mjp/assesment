# Support Ticket Management System

Sibling apps: Spring Boot API (`backend/`) and Next.js UI (`frontend/`). Specs live under `spec/`.

## Quick start

### Backend (Java 21)

```bash
cd backend
export JAVA_HOME=…   # JDK 21+
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

- Health: `http://localhost:8080/actuator/health`
- API base: `http://localhost:8080/api/tickets`
- Local profile uses **file H2** at `backend/data/tickets` (gitignored). Empty password placeholder only.

Postgres (optional):

```bash
# Copy root .env.example → .env and set SPRING_DATASOURCE_* placeholders (never commit real secrets)
./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
```

### Frontend

```bash
cd frontend
cp .env.example .env.local   # NEXT_PUBLIC_API_BASE_URL only — no secrets
npm install
npm run dev
```

Open `http://localhost:3000` → `/tickets`.

### Tests

```bash
cd backend
./mvnw test
```

## T15 — Persistence restart runbook (durable demo)

Proves FR8 with the **local** file-H2 profile (complements automated `TicketPersistenceIT`).

1. Start: `./mvnw spring-boot:run -Dspring-boot.run.profiles=local`
2. Create a ticket and a comment (UI or `curl` against `/api/tickets` and `/api/tickets/{id}/comments`).
3. Stop the JVM (Ctrl+C). Confirm `backend/data/tickets.mv.db` still exists.
4. Start again with the same local profile and datasource path.
5. `GET /api/tickets/{id}` — title/description/assignee and comments are unchanged.

Recorded verification (2026-09-22): create → stop → start against the same file store → ticket + comment still returned (`T15 RESTART OK`).

## Secrets

- Never commit `.env`, `.env.local`, or real DB passwords.
- Use `.env.example` / YAML `${VAR}` placeholders only.
- See plan task **T16** for the hygiene checklist.
