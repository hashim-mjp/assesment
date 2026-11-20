# Ticket status state machine

**Authoritative** allow/deny rules for ticket `status`. The backend **MUST** enforce this machine. Invalid transitions are a hard reject (never coerced, ignored, or “snapped” to another status).

HTTP transport for changes: `POST /api/tickets/{id}/transition` — see `spec/api-contract.md`. This file owns **which** transitions are legal; the contract owns request/response shapes.

**No implementation in this document.** Integration and domain tests must be able to copy the transition table below.

---

## Statuses

| Status | Meaning (brief) |
|--------|-----------------|
| `OPEN` | Newly created; work not started |
| `IN_PROGRESS` | Actively being worked |
| `RESOLVED` | Work complete; awaiting close |
| `CLOSED` | Finished and closed (terminal) |
| `CANCELLED` | Abandoned / voided (terminal) |

API enum values match these strings exactly (`TicketStatus` in `spec/api-contract.md` / `spec/data-model.md`).

---

## Initial status on create

| Rule | Value |
|------|--------|
| Initial status | **`OPEN`** |
| Client-supplied status on create | **Rejected** — create must not accept `status` from the client (`400` `VALIDATION_ERROR` per API contract). Backend always sets `OPEN`. |

Justification: requirements do not define another default; assignment and `spec/api-contract.md` already lock create → `OPEN`.

---

## Allowed transitions (happy paths)

Only these directed edges are allowed (same-status is **not** allowed):

```
OPEN ──────────────► IN_PROGRESS ──────────────► RESOLVED ──────────────► CLOSED
  │                       │
  └───────► CANCELLED ◄───┘
```

Explicit allow list:

| From | To | Notes |
|------|-----|--------|
| `OPEN` | `IN_PROGRESS` | Start work |
| `OPEN` | `CANCELLED` | Cancel before start |
| `IN_PROGRESS` | `RESOLVED` | Mark resolved |
| `IN_PROGRESS` | `CANCELLED` | Cancel while in progress |
| `RESOLVED` | `CLOSED` | Close after resolve |

No other pair is allowed.

---

## Closing after resolve

| Question | Answer |
|----------|--------|
| Is `RESOLVED → CLOSED` the only way to reach `CLOSED`? | **Yes.** `CLOSED` is reachable **only** from `RESOLVED`. There is no `OPEN → CLOSED`, `IN_PROGRESS → CLOSED`, or `CANCELLED → CLOSED`. |
| After resolve, is `RESOLVED → CLOSED` the only legal next step? | **Yes.** From `RESOLVED`, the **only** allowed transition is to `CLOSED`. No reopen, cancel, or skip. |

---

## Terminal statuses

| Status | Terminal? | Further transitions |
|--------|-----------|---------------------|
| `CANCELLED` | **Yes** | None. Any `CANCELLED → *` is **deny**. |
| `CLOSED` | **Yes** | None. Any `CLOSED → *` is **deny**. |
| `OPEN` | No | May go to `IN_PROGRESS` or `CANCELLED`. |
| `IN_PROGRESS` | No | May go to `RESOLVED` or `CANCELLED`. |
| `RESOLVED` | No | May go only to `CLOSED`. |

---

## Illegal examples (must be rejected)

These are **required** deny cases (non-exhaustive; the full matrix below is exhaustive):

| From | To | Result |
|------|-----|--------|
| `CLOSED` | `OPEN` | Deny |
| `RESOLVED` | `OPEN` | Deny |
| `CANCELLED` | `OPEN` | Deny |

Same-status requests (e.g. `OPEN → OPEN`) are also **deny** — not treated as a successful no-op.

---

## Invalid transition — HTTP and error code

Aligned with `spec/api-contract.md` and `rules/api-standards.md`:

| Aspect | Value |
|--------|--------|
| HTTP status | **`422` Unprocessable Entity** |
| Error `code` | **`INVALID_TRANSITION`** |
| Body | Standard error JSON (`timestamp`, `status`, `error`, `code`, `message`, `path`, optional `details`) |
| Behaviour | Do **not** change ticket status; do **not** return `200` with an embedded error |

Unknown / malformed target status strings (not a `TicketStatus` enum value) are **`400` `VALIDATION_ERROR`**, not `INVALID_TRANSITION` (validation vs domain transition).

---

## Enforcement location

| Layer | Role |
|-------|------|
| **Backend domain service** (or equivalent domain/application policy) | **MUST** evaluate current → target against this table and reject illegal transitions. Source of truth at runtime. |
| Persistence / DB | May store the current status; **must not** be the sole enforcer of the allow matrix. |
| UI | May hide illegal actions for UX; **must not** be the only guard. Direct API calls that violate the table must still fail with `422` `INVALID_TRANSITION`. |

Controllers map the domain failure to HTTP; they do not invent alternate transition rules.

---

## Transition table (from × to → allow / deny)

Rows = **from** (current status). Columns = **to** (requested status).

Copy this table into tests. `ALLOW` = succeed and persist new status. `DENY` = `422` `INVALID_TRANSITION`, status unchanged.

| from \ to | OPEN | IN_PROGRESS | RESOLVED | CLOSED | CANCELLED |
|-----------|------|-------------|----------|--------|-----------|
| **OPEN** | DENY | ALLOW | DENY | DENY | ALLOW |
| **IN_PROGRESS** | DENY | DENY | ALLOW | DENY | ALLOW |
| **RESOLVED** | DENY | DENY | DENY | ALLOW | DENY |
| **CLOSED** | DENY | DENY | DENY | DENY | DENY |
| **CANCELLED** | DENY | DENY | DENY | DENY | DENY |

### Compact allow list (for assertion helpers)

```
OPEN → IN_PROGRESS
OPEN → CANCELLED
IN_PROGRESS → RESOLVED
IN_PROGRESS → CANCELLED
RESOLVED → CLOSED
```

Everything else in the 5×5 matrix is DENY (25 − 5 = 20 denies), including all diagonal same-status cells and all transitions out of `CLOSED` / `CANCELLED`.

---

## Traceability

| Concern | Covered here |
|---------|----------------|
| Initial create status | `OPEN` |
| Happy path chain | `OPEN → IN_PROGRESS → RESOLVED → CLOSED` |
| Early cancel | `OPEN → CANCELLED`, `IN_PROGRESS → CANCELLED` |
| Terminals | `CLOSED`, `CANCELLED` |
| Reopen / illegal examples | Denied in matrix |
| API rejection | `422` + `INVALID_TRANSITION` |
| Backend (not UI-only) | Domain service enforcement |
| FR / acceptance | Valid transitions succeed; invalid rejected by backend; state-machine integration tests can enumerate this table |

---

## Related artefacts

| Artefact | Relationship |
|----------|----------------|
| `spec/requirements.md` | Status lifecycle requirement; acceptance criteria |
| `spec/api-contract.md` | `POST .../transition`, error body, create = `OPEN` |
| `spec/data-model.md` | `TicketStatus` enum / column |
| `spec/architecture.md` | Domain owns transition rules |
| `rules/testing.md` | Every ALLOW and DENY cell must be tested against the backend |
