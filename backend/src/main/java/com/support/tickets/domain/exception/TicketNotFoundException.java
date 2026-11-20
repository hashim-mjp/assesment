package com.support.tickets.domain.exception;

import java.util.UUID;

/**
 * Raised when a ticket id does not exist. Mapped to HTTP {@code 404} with {@code NOT_FOUND}.
 */
public class TicketNotFoundException extends RuntimeException {

	private final UUID ticketId;

	public TicketNotFoundException(UUID ticketId) {
		super("Ticket not found: " + ticketId);
		this.ticketId = ticketId;
	}

	public UUID getTicketId() {
		return ticketId;
	}
}
