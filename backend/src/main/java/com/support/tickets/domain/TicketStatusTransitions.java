package com.support.tickets.domain;

import com.support.tickets.domain.exception.InvalidTransitionException;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Authoritative ticket status allow/deny matrix from {@code spec/state-machine.md}.
 * Same-status and any pair not listed is denied.
 */
public final class TicketStatusTransitions {

	private static final Map<TicketStatus, Set<TicketStatus>> ALLOWED = new EnumMap<>(TicketStatus.class);

	static {
		ALLOWED.put(TicketStatus.OPEN, EnumSet.of(TicketStatus.IN_PROGRESS, TicketStatus.CANCELLED));
		ALLOWED.put(TicketStatus.IN_PROGRESS, EnumSet.of(TicketStatus.RESOLVED, TicketStatus.CANCELLED));
		ALLOWED.put(TicketStatus.RESOLVED, EnumSet.of(TicketStatus.CLOSED));
		ALLOWED.put(TicketStatus.CLOSED, EnumSet.noneOf(TicketStatus.class));
		ALLOWED.put(TicketStatus.CANCELLED, EnumSet.noneOf(TicketStatus.class));
	}

	private TicketStatusTransitions() {
	}

	public static boolean isAllowed(TicketStatus from, TicketStatus to) {
		if (from == null || to == null) {
			return false;
		}
		Set<TicketStatus> targets = ALLOWED.get(from);
		return targets != null && targets.contains(to);
	}

	/**
	 * @throws InvalidTransitionException if {@code from → to} is not allowed
	 */
	public static void assertAllowed(TicketStatus from, TicketStatus to) {
		if (!isAllowed(from, to)) {
			throw new InvalidTransitionException(from, to);
		}
	}
}
