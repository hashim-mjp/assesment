package com.support.tickets.api.dto.response;

import java.time.Instant;
import java.util.List;

/**
 * Stable error JSON per {@code spec/api-contract.md} / {@code rules/api-standards.md}.
 */
public record ErrorResponse(
		Instant timestamp,
		int status,
		String error,
		String code,
		String message,
		String path,
		List<ErrorDetail> details) {
}
