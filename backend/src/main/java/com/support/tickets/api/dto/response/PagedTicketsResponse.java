package com.support.tickets.api.dto.response;

import java.util.List;

/** Paged list envelope per api-contract. */
public record PagedTicketsResponse(
		List<TicketSummaryResponse> content,
		int page,
		int size,
		long totalElements,
		int totalPages) {
}
