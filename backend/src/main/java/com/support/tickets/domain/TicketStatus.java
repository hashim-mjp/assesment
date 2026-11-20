package com.support.tickets.domain;

/**
 * Ticket lifecycle status. Transition rules live in {@code spec/state-machine.md}
 * and are enforced by {@link TicketStatusTransitions}.
 */
public enum TicketStatus {
	OPEN,
	IN_PROGRESS,
	RESOLVED,
	CLOSED,
	CANCELLED
}
