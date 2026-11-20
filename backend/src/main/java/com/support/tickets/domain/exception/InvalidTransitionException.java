package com.support.tickets.domain.exception;

import com.support.tickets.domain.TicketStatus;

/**
 * Raised when a ticket status change violates {@link com.support.tickets.domain.TicketStatusTransitions}.
 * Mapped to HTTP {@code 422} with {@code INVALID_TRANSITION} by the API layer.
 */
public class InvalidTransitionException extends RuntimeException {

	private final TicketStatus from;
	private final TicketStatus to;

	public InvalidTransitionException(TicketStatus from, TicketStatus to) {
		super("Cannot transition from " + from + " to " + to);
		this.from = from;
		this.to = to;
	}

	public TicketStatus getFrom() {
		return from;
	}

	public TicketStatus getTo() {
		return to;
	}
}
