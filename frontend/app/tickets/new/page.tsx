"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState, type FormEvent } from "react";
import { ErrorAlert } from "@/components/ErrorAlert";
import {
  ApiRequestError,
  createTicket,
  fieldErrorsFrom,
  type TicketPriority,
} from "@/lib/api";

const PRIORITIES: TicketPriority[] = ["LOW", "MEDIUM", "HIGH", "URGENT"];

/**
 * Create ticket — POST /api/tickets (T11).
 * No status control; server always creates OPEN.
 */
export default function NewTicketPage() {
  const router = useRouter();
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [priority, setPriority] = useState<TicketPriority>("MEDIUM");
  const [assignee, setAssignee] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<ApiRequestError | string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    setFieldErrors({});

    const body: {
      title: string;
      description: string;
      priority: TicketPriority;
      assignee?: string;
    } = {
      title: title.trim(),
      description: description.trim(),
      priority,
    };
    const a = assignee.trim();
    if (a) body.assignee = a;

    try {
      const ticket = await createTicket(body);
      router.push(`/tickets/${ticket.id}`);
    } catch (err) {
      if (err instanceof ApiRequestError) {
        setError(err);
        setFieldErrors(fieldErrorsFrom(err));
      } else {
        setError("Could not reach the server");
      }
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="page">
      <div className="page-header">
        <h1>New ticket</h1>
        <Link className="button" href="/tickets">
          Cancel
        </Link>
      </div>

      <ErrorAlert error={error} />

      <form className="form" onSubmit={onSubmit} noValidate>
        <div className="field">
          <label htmlFor="title">Title</label>
          <input
            id="title"
            name="title"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            maxLength={200}
            required
            disabled={submitting}
            aria-invalid={Boolean(fieldErrors.title)}
          />
          {fieldErrors.title && (
            <p className="field-error">{fieldErrors.title}</p>
          )}
        </div>

        <div className="field">
          <label htmlFor="description">Description</label>
          <textarea
            id="description"
            name="description"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            rows={6}
            maxLength={10000}
            required
            disabled={submitting}
            aria-invalid={Boolean(fieldErrors.description)}
          />
          {fieldErrors.description && (
            <p className="field-error">{fieldErrors.description}</p>
          )}
        </div>

        <div className="field">
          <label htmlFor="priority">Priority</label>
          <select
            id="priority"
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
          <p className="field-hint">Defaults to MEDIUM if unchanged.</p>
          {fieldErrors.priority && (
            <p className="field-error">{fieldErrors.priority}</p>
          )}
        </div>

        <div className="field">
          <label htmlFor="assignee">Assignee (optional)</label>
          <input
            id="assignee"
            name="assignee"
            value={assignee}
            onChange={(e) => setAssignee(e.target.value)}
            maxLength={120}
            disabled={submitting}
            placeholder="e.g. alex@example.com"
            aria-invalid={Boolean(fieldErrors.assignee)}
          />
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
            {submitting ? "Creating…" : "Create ticket"}
          </button>
          <Link className="button" href="/tickets">
            Cancel
          </Link>
        </div>
      </form>
    </div>
  );
}
