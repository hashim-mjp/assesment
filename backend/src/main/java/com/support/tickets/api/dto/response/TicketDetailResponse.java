package com.support.tickets.api.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.support.tickets.domain.TicketPriority;
import com.support.tickets.domain.TicketStatus;

/** Detail shape = summary fields + description + comments. */
public record TicketDetailResponse(
		UUID id,
		String title,
		String description,
		TicketPriority priority,
		String assignee,
		TicketStatus status,
		Instant createdAt,
		Instant updatedAt,
		List<CommentResponse> comments) {
}
