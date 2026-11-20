"use client";

import Link from "next/link";
import { usePathname, useRouter, useSearchParams } from "next/navigation";
import {
  FormEvent,
  Suspense,
  useCallback,
  useEffect,
  useState,
} from "react";
import { ErrorAlert } from "@/components/ErrorAlert";
import { StatusBadge } from "@/components/StatusBadge";
import {
  ALL_TICKET_STATUSES,
  ApiRequestError,
  listTickets,
  type PagedTickets,
  type TicketStatus,
} from "@/lib/api";

/**
 * Ticket list — GET /api/tickets with q / status / page (T12).
 * Query params sync so refresh/share preserve search and filter.
 */
export default function TicketsListPage() {
  return (
    <Suspense fallback={<p className="muted">Loading tickets…</p>}>
      <TicketsList />
    </Suspense>
  );
}

function TicketsList() {
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();

  const qParam = searchParams.get("q") ?? "";
  const statusParam = (searchParams.get("status") ?? "") as TicketStatus | "";
  const pageParam = Math.max(0, Number(searchParams.get("page") ?? "0") || 0);

  const [qDraft, setQDraft] = useState(qParam);
  const [data, setData] = useState<PagedTickets | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<ApiRequestError | string | null>(null);

  useEffect(() => {
    setQDraft(qParam);
  }, [qParam]);

  const replaceParams = useCallback(
    (next: { q?: string; status?: string; page?: number }) => {
      const params = new URLSearchParams(searchParams.toString());

      const q =
        next.q !== undefined ? next.q.trim() : (params.get("q") ?? "").trim();
      const status =
        next.status !== undefined
          ? next.status
          : (params.get("status") ?? "");
      const page =
        next.page !== undefined
          ? next.page
          : Number(params.get("page") ?? "0") || 0;

      if (q) params.set("q", q);
      else params.delete("q");

      if (status) params.set("status", status);
      else params.delete("status");

      if (page > 0) params.set("page", String(page));
      else params.delete("page");

      const qs = params.toString();
      router.replace(qs ? `${pathname}?${qs}` : pathname);
    },
    [pathname, router, searchParams]
  );

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const result = await listTickets({
        page: pageParam,
        size: 20,
        q: qParam || undefined,
        status: statusParam || undefined,
      });
      setData(result);
    } catch (e) {
      setData(null);
      setError(
        e instanceof ApiRequestError ? e : "Could not reach the server"
      );
    } finally {
      setLoading(false);
    }
  }, [pageParam, qParam, statusParam]);

  useEffect(() => {
    void load();
  }, [load]);

  function onSearchSubmit(e: FormEvent) {
    e.preventDefault();
    replaceParams({ q: qDraft, page: 0 });
  }

  function onStatusChange(value: string) {
    replaceParams({ status: value, page: 0 });
  }

  function clearFilters() {
    setQDraft("");
    replaceParams({ q: "", status: "", page: 0 });
  }

  const hasFilters = Boolean(qParam.trim() || statusParam);
  const empty = data && data.totalElements === 0;

  return (
    <div className="page">
      <div className="page-header">
        <h1>Tickets</h1>
        <Link className="button primary" href="/tickets/new">
          New ticket
        </Link>
      </div>

      <form className="list-controls" onSubmit={onSearchSubmit}>
        <div className="field">
          <label htmlFor="q">Search</label>
          <input
            id="q"
            name="q"
            type="search"
            value={qDraft}
            onChange={(e) => setQDraft(e.target.value)}
            placeholder="Title or description"
            disabled={loading}
          />
        </div>
        <div className="field">
          <label htmlFor="status">Status</label>
          <select
            id="status"
            name="status"
            value={statusParam}
            onChange={(e) => onStatusChange(e.target.value)}
            disabled={loading}
          >
            <option value="">All</option>
            {ALL_TICKET_STATUSES.map((s) => (
              <option key={s} value={s}>
                {s}
              </option>
            ))}
          </select>
        </div>
        <div className="list-controls-actions">
          <button type="submit" className="button primary" disabled={loading}>
            Search
          </button>
          {hasFilters && (
            <button
              type="button"
              className="button"
              disabled={loading}
              onClick={clearFilters}
            >
              Clear
            </button>
          )}
        </div>
      </form>

      <ErrorAlert error={error} />
      {error && (
        <p>
          <button
            type="button"
            className="button"
            disabled={loading}
            onClick={() => void load()}
          >
            Retry
          </button>
        </p>
      )}

      {loading && !data && <p className="muted">Loading tickets…</p>}

      {!loading && empty && !hasFilters && (
        <div className="empty-state">
          <p>No tickets yet. Create one to get started.</p>
          <Link href="/tickets/new">New ticket</Link>
        </div>
      )}

      {!loading && empty && hasFilters && (
        <div className="empty-state">
          <p>No tickets match your search or filter.</p>
          <button type="button" className="button" onClick={clearFilters}>
            Clear search and filter
          </button>
        </div>
      )}

      {data && data.content.length > 0 && (
        <>
          <div className="table-wrap">
            <table className="ticket-table">
              <thead>
                <tr>
                  <th>Title</th>
                  <th>Status</th>
                  <th>Priority</th>
                  <th>Assignee</th>
                  <th>Created</th>
                </tr>
              </thead>
              <tbody>
                {data.content.map((t) => (
                  <tr key={t.id}>
                    <td>
                      <Link href={`/tickets/${t.id}`}>{t.title}</Link>
                    </td>
                    <td>
                      <StatusBadge status={t.status} />
                    </td>
                    <td>{t.priority}</td>
                    <td>{t.assignee ?? "—"}</td>
                    <td className="muted">{formatWhen(t.createdAt)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {data.totalPages > 1 && (
            <div className="pagination">
              <button
                type="button"
                className="button"
                disabled={pageParam <= 0 || loading}
                onClick={() =>
                  replaceParams({ page: Math.max(0, pageParam - 1) })
                }
              >
                Previous
              </button>
              <span className="muted">
                Page {data.page + 1} of {data.totalPages} ({data.totalElements}{" "}
                total)
              </span>
              <button
                type="button"
                className="button"
                disabled={pageParam >= data.totalPages - 1 || loading}
                onClick={() => replaceParams({ page: pageParam + 1 })}
              >
                Next
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
}

function formatWhen(iso: string): string {
  try {
    return new Date(iso).toLocaleString();
  } catch {
    return iso;
  }
}
