package com.support.tickets.api.dto.response;

import java.time.Instant;
import java.util.UUID;

/** Comment payload for ticket detail and add-comment responses. */
public record CommentResponse(
		UUID id,
		String body,
		String author,
		Instant createdAt) {
}
