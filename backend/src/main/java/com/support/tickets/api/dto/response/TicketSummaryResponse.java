package com.support.tickets.api.dto.response;

import java.time.Instant;
import java.util.UUID;

import com.support.tickets.domain.TicketPriority;
import com.support.tickets.domain.TicketStatus;

/** List-item shape per api-contract ticket summary. */
public record TicketSummaryResponse(
		UUID id,
		String title,
		TicketPriority priority,
		String assignee,
		TicketStatus status,
		Instant createdAt,
		Instant updatedAt) {
}
