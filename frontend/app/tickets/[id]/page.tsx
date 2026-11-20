"use client";

import Link from "next/link";
import { useParams } from "next/navigation";
import {
  useCallback,
  useEffect,
  useState,
  type FormEvent,
} from "react";
import { ErrorAlert } from "@/components/ErrorAlert";
import { StatusBadge } from "@/components/StatusBadge";
import {
  addComment,
  allowedTransitionsFrom,
  ApiRequestError,
  fieldErrorsFrom,
  getTicket,
  patchTicket,
  transitionTicket,
  type TicketDetail,
  type TicketPriority,
  type TicketStatus,
} from "@/lib/api";

const PRIORITIES: TicketPriority[] = ["LOW", "MEDIUM", "HIGH", "URGENT"];

const STATUS_LABELS: Record<TicketStatus, string> = {
  OPEN: "Open",
  IN_PROGRESS: "In progress",
  RESOLVED: "Resolved",
  CLOSED: "Closed",
  CANCELLED: "Cancelled",
};

/**
 * Ticket detail — GET + PATCH + transition + comments (T12).
 * Illegal status actions may be hidden; backend still enforces with 422.
 */
export default function TicketDetailPage() {
  const params = useParams();
  const id = typeof params.id === "string" ? params.id : "";

  const [ticket, setTicket] = useState<TicketDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<ApiRequestError | string | null>(null);
  const [notFound, setNotFound] = useState(false);

  const load = useCallback(async () => {
    if (!id) return;
    setLoading(true);
    setError(null);
    setNotFound(false);
    try {
      const result = await getTicket(id);
      setTicket(result);
    } catch (e) {
      setTicket(null);
      if (e instanceof ApiRequestError) {
        if (e.status === 404 || e.code === "NOT_FOUND") {
          setNotFound(true);
        } else {
          setError(e);
        }
      } else {
        setError("Could not reach the server");
      }
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    void load();
  }, [load]);

  if (loading && !ticket) {
    return (
      <div className="page">
        <p className="muted">Loading ticket…</p>
      </div>
    );
  }

  if (notFound) {
    return (
      <div className="page">
        <h1>Ticket not found</h1>
        <p className="muted">
          No ticket exists with id <code>{id}</code>.
        </p>
        <p>
          <Link href="/tickets">← Back to list</Link>
        </p>
      </div>
    );
  }

  if (error && !ticket) {
    return (
      <div className="page">
        <div className="page-header">
          <h1>Ticket detail</h1>
          <Link href="/tickets">← Back to list</Link>
        </div>
        <ErrorAlert error={error} />
        <button type="button" className="button" onClick={() => void load()}>
          Retry
        </button>
      </div>
    );
  }

  if (!ticket) {
    return null;
  }

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <p className="breadcrumb">
            <Link href="/tickets">Tickets</Link>
            <span aria-hidden="true"> / </span>
            <span className="muted">{ticket.id.slice(0, 8)}…</span>
          </p>
          <h1>{ticket.title}</h1>
        </div>
        <StatusBadge status={ticket.status} />
      </div>

      <dl className="detail-grid">
        <div>
          <dt>Priority</dt>
          <dd>{ticket.priority}</dd>
        </div>
        <div>
          <dt>Assignee</dt>
          <dd>{ticket.assignee ?? "Unassigned"}</dd>
        </div>
        <div>
          <dt>Created</dt>
          <dd>{formatWhen(ticket.createdAt)}</dd>
        </div>
        <div>
          <dt>Updated</dt>
          <dd>{formatWhen(ticket.updatedAt)}</dd>
        </div>
      </dl>

      <section className="section">
        <h2>Description</h2>
        <p className="description-body">{ticket.description}</p>
      </section>

      <EditFieldsSection ticket={ticket} onUpdated={setTicket} />

      <StatusTransitionSection ticket={ticket} onUpdated={setTicket} />

      <CommentsSection ticket={ticket} onUpdated={setTicket} />
    </div>
  );
}

function EditFieldsSection({
  ticket,
  onUpdated,
}: {
  ticket: TicketDetail;
  onUpdated: (t: TicketDetail) => void;
}) {
  const [title, setTitle] = useState(ticket.title);
  const [description, setDescription] = useState(ticket.description);
  const [priority, setPriority] = useState<TicketPriority>(ticket.priority);
  const [assignee, setAssignee] = useState(ticket.assignee ?? "");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<ApiRequestError | string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [savedHint, setSavedHint] = useState(false);

  useEffect(() => {
    setTitle(ticket.title);
    setDescription(ticket.description);
    setPriority(ticket.priority);
    setAssignee(ticket.assignee ?? "");
  }, [ticket.id, ticket.title, ticket.description, ticket.priority, ticket.assignee]);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    setFieldErrors({});
    setSavedHint(false);

    const body: {
      title?: string;
      description?: string;
      priority?: TicketPriority;
      assignee?: string | null;
    } = {};

    const nextTitle = title.trim();
    const nextDescription = description.trim();
    const nextAssignee = assignee.trim();

    if (nextTitle !== ticket.title) body.title = nextTitle;
    if (nextDescription !== ticket.description) body.description = nextDescription;
    if (priority !== ticket.priority) body.priority = priority;

    const currentAssignee = ticket.assignee ?? "";
    if (nextAssignee !== currentAssignee) {
      body.assignee = nextAssignee === "" ? null : nextAssignee;
    }

    if (Object.keys(body).length === 0) {
      setError("Change at least one field before saving.");
      setSubmitting(false);
      return;
    }

    try {
      const updated = await patchTicket(ticket.id, body);
      onUpdated(updated);
      setSavedHint(true);
    } catch (err) {
      if (err instanceof ApiRequestError) {
        if (err.status === 404 || err.code === "NOT_FOUND") {
          setError(err);
        } else {
          setError(err);
          setFieldErrors(fieldErrorsFrom(err));
        }
      } else {
        setError("Could not reach the server");
      }
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className="section">
      <h2>Edit fields</h2>
      <ErrorAlert error={error} />
      {savedHint && (
        <p className="alert alert-success" role="status">
          Ticket updated.
        </p>
      )}
      <form className="form" onSubmit={onSubmit} noValidate>
        <div className="field">
          <label htmlFor="edit-title">Title</label>
          <input
            id="edit-title"
            name="title"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            maxLength={200}
            disabled={submitting}
            aria-invalid={Boolean(fieldErrors.title)}
          />
          {fieldErrors.title && (
            <p className="field-error">{fieldErrors.title}</p>
          )}
        </div>

        <div className="field">
          <label htmlFor="edit-description">Description</label>
          <textarea
            id="edit-description"
            name="description"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            rows={5}
            maxLength={10000}
            disabled={submitting}
            aria-invalid={Boolean(fieldErrors.description)}
          />
          {fieldErrors.description && (
            <p className="field-error">{fieldErrors.description}</p>
          )}
        </div>

        <div className="field">
          <label htmlFor="edit-priority">Priority</label>
          <select
            id="edit-priority"
            name="priority"
            value={priority}
            onChange={(e) => setPriority(e.target.value as TicketPriority)}
            disabled={submitting}
          >
            {PRIORITIES.map((p) => (
              <option key={p} value={p}>
                {p}
              </option>
            ))}
          </select>
          {fieldErrors.priority && (
            <p className="field-error">{fieldErrors.priority}</p>
          )}
        </div>

        <div className="field">
          <label htmlFor="edit-assignee">Assignee</label>
          <input
            id="edit-assignee"
            name="assignee"
            value={assignee}
            onChange={(e) => setAssignee(e.target.value)}
            maxLength={120}
            disabled={submitting}
            placeholder="Leave blank to unassign"
            aria-invalid={Boolean(fieldErrors.assignee)}
          />
          <p className="field-hint">Clear the field and save to unassign.</p>
          {fieldErrors.assignee && (
            <p className="field-error">{fieldErrors.assignee}</p>
          )}
        </div>

        <div className="form-actions">
          <button
            type="submit"
            className="button primary"
            disabled={submitting}
          >
            {submitting ? "Saving…" : "Save changes"}
          </button>
        </div>
      </form>
    </section>
  );
}

function StatusTransitionSection({
  ticket,
  onUpdated,
}: {
  ticket: TicketDetail;
  onUpdated: (t: TicketDetail) => void;
}) {
  const allowed = allowedTransitionsFrom(ticket.status);
  const [busyTarget, setBusyTarget] = useState<TicketStatus | null>(null);
  const [error, setError] = useState<ApiRequestError | string | null>(null);

  async function onTransition(target: TicketStatus) {
    setBusyTarget(target);
    setError(null);
    try {
      const updated = await transitionTicket(ticket.id, target);
      onUpdated(updated);
    } catch (err) {
      // Do not change displayed status on failure (backend is source of truth).
      if (err instanceof ApiRequestError) {
        setError(err);
      } else {
        setError("Could not reach the server");
      }
    } finally {
      setBusyTarget(null);
    }
  }

  return (
    <section className="section">
      <h2>Status</h2>
      <p>
        Current: <StatusBadge status={ticket.status} />
      </p>
      <ErrorAlert error={error} />

      {allowed.length === 0 ? (
        <p className="muted">
          No further transitions — this status is terminal.
        </p>
      ) : (
        <div className="transition-actions">
          {allowed.map((target) => (
            <button
              key={target}
              type="button"
              className="button"
              disabled={busyTarget !== null}
              onClick={() => void onTransition(target)}
            >
              {busyTarget === target
                ? "Updating…"
                : `Move to ${STATUS_LABELS[target]}`}
            </button>
          ))}
        </div>
      )}
      <p className="field-hint">
        Only allowed targets are shown. The API still rejects illegal
        transitions with 422 if requested another way.
      </p>
    </section>
  );
}

function CommentsSection({
  ticket,
  onUpdated,
}: {
  ticket: TicketDetail;
  onUpdated: (t: TicketDetail) => void;
}) {
  const [body, setBody] = useState("");
  const [author, setAuthor] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<ApiRequestError | string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    setFieldErrors({});

    const payload: { body: string; author?: string } = {
      body: body.trim(),
    };
    const a = author.trim();
    if (a) payload.author = a;

    try {
      const comment = await addComment(ticket.id, payload);
      onUpdated({
        ...ticket,
        comments: [...ticket.comments, comment],
      });
      setBody("");
    } catch (err) {
      if (err instanceof ApiRequestError) {
        if (err.status === 404 || err.code === "NOT_FOUND") {
          setError(err);
        } else {
          setError(err);
          setFieldErrors(fieldErrorsFrom(err));
        }
      } else {
        setError("Could not reach the server");
      }
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className="section">
      <h2>Comments</h2>
      {ticket.comments.length === 0 ? (
        <p className="muted">No comments yet.</p>
      ) : (
        <ul className="comment-list">
          {ticket.comments.map((c) => (
            <li key={c.id} className="comment">
              <p className="comment-meta muted">
                {c.author ?? "Anonymous"} · {formatWhen(c.createdAt)}
              </p>
              <p>{c.body}</p>
            </li>
          ))}
        </ul>
      )}

      <ErrorAlert error={error} />

      <form className="form comment-form" onSubmit={onSubmit} noValidate>
        <div className="field">
          <label htmlFor="comment-body">Add a comment</label>
          <textarea
            id="comment-body"
            name="body"
            value={body}
            onChange={(e) => setBody(e.target.value)}
            rows={3}
            maxLength={5000}
            disabled={submitting}
            required
            aria-invalid={Boolean(fieldErrors.body)}
          />
          {fieldErrors.body && (
            <p className="field-error">{fieldErrors.body}</p>
          )}
        </div>
        <div className="field">
          <label htmlFor="comment-author">Author (optional)</label>
          <input
            id="comment-author"
            name="author"
            value={author}
            onChange={(e) => setAuthor(e.target.value)}
            maxLength={120}
            disabled={submitting}
            aria-invalid={Boolean(fieldErrors.author)}
          />
          {fieldErrors.author && (
            <p className="field-error">{fieldErrors.author}</p>
          )}
        </div>
        <div className="form-actions">
          <button
            type="submit"
            className="button primary"
            disabled={submitting}
          >
            {submitting ? "Posting…" : "Add comment"}
          </button>
        </div>
      </form>
    </section>
  );
}

function formatWhen(iso: string): string {
  try {
    return new Date(iso).toLocaleString();
  } catch {
    return iso;
  }
}
