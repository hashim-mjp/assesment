package com.support.tickets.api.dto.request;

import com.support.tickets.domain.TicketStatus;

import jakarta.validation.constraints.NotNull;

/**
 * Body for {@code POST /api/tickets/{id}/transition}.
 * Missing or non-enum {@code status} is rejected at the API boundary
 * ({@code @NotNull} / Jackson deserialize failure → {@code 400}).
 */
public record TransitionTicketRequest(
		@NotNull(message = "Status is required") TicketStatus status) {
}
