package com.support.tickets.domain;

/**
 * Ticket priority values locked in {@code spec/api-contract.md}.
 * Default on create (API) is {@link #MEDIUM}.
 */
public enum TicketPriority {
	LOW,
	MEDIUM,
	HIGH,
	URGENT
}
