# Frontend — Support Ticket Management System

Next.js (App Router) thin client for the Spring Boot API.

## Setup

```bash
cp .env.example .env.local
npm install
npm run dev
```

Open [http://localhost:3000](http://localhost:3000) (redirects to `/tickets`).

`NEXT_PUBLIC_API_BASE_URL` points at the backend (default `http://localhost:8080`). Do not put secrets in frontend env files.

## Routes

| Route | Purpose |
|-------|---------|
| `/` | Redirects to `/tickets` |
| `/tickets` | List tickets (`GET /api/tickets`); keyword search `q` + status filter (URL sync) |
| `/tickets/new` | Create form (`POST /api/tickets`) |
| `/tickets/[id]` | Detail (`GET`); edit fields (`PATCH`); status transitions; comments |
