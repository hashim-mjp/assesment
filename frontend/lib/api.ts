/**
 * Thin REST client for the Support Ticket API.
 * Base URL from env — never hardcode secrets here.
 */

export type TicketStatus =
  | "OPEN"
  | "IN_PROGRESS"
  | "RESOLVED"
  | "CLOSED"
  | "CANCELLED";

export type TicketPriority = "LOW" | "MEDIUM" | "HIGH" | "URGENT";

export type TicketSummary = {
  id: string;
  title: string;
  priority: TicketPriority;
  assignee: string | null;
  status: TicketStatus;
  createdAt: string;
  updatedAt: string;
};

export type Comment = {
  id: string;
  body: string;
  author: string | null;
  createdAt: string;
};

export type TicketDetail = TicketSummary & {
  description: string;
  comments: Comment[];
};

export type PagedTickets = {
  content: TicketSummary[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type FieldErrorDetail = {
  field: string;
  message: string;
  rejectedValue?: unknown;
};

export type ApiErrorBody = {
  timestamp?: string;
  status: number;
  error?: string;
  code?: string;
  message: string;
  path?: string;
  details?: FieldErrorDetail[];
};

export class ApiRequestError extends Error {
  readonly status: number;
  readonly code?: string;
  readonly details: FieldErrorDetail[];
  readonly network: boolean;

  constructor(opts: {
    message: string;
    status: number;
    code?: string;
    details?: FieldErrorDetail[];
    network?: boolean;
  }) {
    super(opts.message);
    this.name = "ApiRequestError";
    this.status = opts.status;
    this.code = opts.code;
    this.details = opts.details ?? [];
    this.network = opts.network ?? false;
  }
}

export function getApiBaseUrl(): string {
  const base = process.env.NEXT_PUBLIC_API_BASE_URL;
  if (!base || base.trim() === "") {
    throw new Error(
      "NEXT_PUBLIC_API_BASE_URL is not set. Copy frontend/.env.example to frontend/.env.local."
    );
  }
  return base.replace(/\/$/, "");
}

export type ListTicketsParams = {
  page?: number;
  size?: number;
  q?: string;
  status?: TicketStatus | "";
};

export type CreateTicketBody = {
  title: string;
  description: string;
  priority?: TicketPriority;
  assignee?: string | null;
};

async function parseError(res: Response): Promise<ApiRequestError> {
  let body: ApiErrorBody | null = null;
  try {
    body = (await res.json()) as ApiErrorBody;
  } catch {
    body = null;
  }
  return new ApiRequestError({
    message:
      body?.message?.trim() ||
      `Request failed with status ${res.status}`,
    status: body?.status ?? res.status,
    code: body?.code,
    details: body?.details ?? [],
  });
}

async function apiFetch<T>(
  path: string,
  init?: RequestInit
): Promise<T> {
  let url: string;
  try {
    url = `${getApiBaseUrl()}${path}`;
  } catch (e) {
    throw new ApiRequestError({
      message:
        e instanceof Error
          ? e.message
          : "API base URL is not configured",
      status: 0,
      network: true,
    });
  }

  let res: Response;
  try {
    res = await fetch(url, {
      ...init,
      headers: {
        Accept: "application/json",
        ...(init?.body ? { "Content-Type": "application/json" } : {}),
        ...init?.headers,
      },
    });
  } catch {
    throw new ApiRequestError({
      message: "Could not reach the server",
      status: 0,
      network: true,
    });
  }

  if (!res.ok) {
    throw await parseError(res);
  }

  if (res.status === 204) {
    return undefined as T;
  }

  return (await res.json()) as T;
}

function buildQuery(params: Record<string, string | number | undefined>): string {
  const qs = new URLSearchParams();
  for (const [key, value] of Object.entries(params)) {
    if (value === undefined || value === "") continue;
    qs.set(key, String(value));
  }
  const s = qs.toString();
  return s ? `?${s}` : "";
}

export function listTickets(
  params: ListTicketsParams = {}
): Promise<PagedTickets> {
  const q = params.q?.trim() || undefined;
  const query = buildQuery({
    page: params.page ?? 0,
    size: params.size ?? 20,
    q,
    status: params.status || undefined,
  });
  return apiFetch<PagedTickets>(`/api/tickets${query}`);
}

export function getTicket(id: string): Promise<TicketDetail> {
  return apiFetch<TicketDetail>(`/api/tickets/${encodeURIComponent(id)}`);
}

export function createTicket(body: CreateTicketBody): Promise<TicketDetail> {
  return apiFetch<TicketDetail>("/api/tickets", {
    method: "POST",
    body: JSON.stringify(body),
  });
}

export type PatchTicketBody = {
  title?: string;
  description?: string;
  priority?: TicketPriority;
  /** `null` or `""` unassigns; omit to leave unchanged. */
  assignee?: string | null;
};

export function patchTicket(
  id: string,
  body: PatchTicketBody
): Promise<TicketDetail> {
  return apiFetch<TicketDetail>(`/api/tickets/${encodeURIComponent(id)}`, {
    method: "PATCH",
    body: JSON.stringify(body),
  });
}

export function transitionTicket(
  id: string,
  status: TicketStatus
): Promise<TicketDetail> {
  return apiFetch<TicketDetail>(
    `/api/tickets/${encodeURIComponent(id)}/transition`,
    {
      method: "POST",
      body: JSON.stringify({ status }),
    }
  );
}

export type AddCommentBody = {
  body: string;
  author?: string | null;
};

export function addComment(
  ticketId: string,
  body: AddCommentBody
): Promise<Comment> {
  return apiFetch<Comment>(
    `/api/tickets/${encodeURIComponent(ticketId)}/comments`,
    {
      method: "POST",
      body: JSON.stringify(body),
    }
  );
}

/**
 * UX helper only — mirrors `state-machine.md` allow list.
 * Backend remains authoritative; illegal transitions still return 422.
 */
const ALLOWED_TRANSITIONS: Record<TicketStatus, TicketStatus[]> = {
  OPEN: ["IN_PROGRESS", "CANCELLED"],
  IN_PROGRESS: ["RESOLVED", "CANCELLED"],
  RESOLVED: ["CLOSED"],
  CLOSED: [],
  CANCELLED: [],
};

export function allowedTransitionsFrom(
  current: TicketStatus
): TicketStatus[] {
  return ALLOWED_TRANSITIONS[current] ?? [];
}

export const ALL_TICKET_STATUSES: TicketStatus[] = [
  "OPEN",
  "IN_PROGRESS",
  "RESOLVED",
  "CLOSED",
  "CANCELLED",
];

/** Map API field details to a record for form controls. */
export function fieldErrorsFrom(
  err: ApiRequestError
): Record<string, string> {
  const map: Record<string, string> = {};
  for (const d of err.details) {
    if (d.field && !map[d.field]) {
      map[d.field] = d.message;
    }
  }
  return map;
}
