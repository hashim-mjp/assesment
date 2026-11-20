package com.support.tickets.api.dto.response;

/**
 * Optional field-level entry in the API error body ({@code details[]}).
 */
public record ErrorDetail(
		String field,
		String message,
		Object rejectedValue) {
}
