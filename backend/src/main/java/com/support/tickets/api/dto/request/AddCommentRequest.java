package com.support.tickets.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Add-comment body. {@code body} is required and non-blank; {@code author} is optional.
 */
public record AddCommentRequest(
		@NotBlank @Size(max = 5_000) String body,
		@Size(max = 120) String author) {
}
