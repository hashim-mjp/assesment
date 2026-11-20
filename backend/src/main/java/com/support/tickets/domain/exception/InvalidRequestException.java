package com.support.tickets.domain.exception;

/**
 * Raised for client request problems detected outside Bean Validation
 * (e.g. list query params). Mapped to HTTP {@code 400} with {@code VALIDATION_ERROR}.
 */
public class InvalidRequestException extends RuntimeException {

	private final String field;
	private final Object rejectedValue;

	public InvalidRequestException(String message) {
		this(message, null, null);
	}

	public InvalidRequestException(String message, String field, Object rejectedValue) {
		super(message);
		this.field = field;
		this.rejectedValue = rejectedValue;
	}

	public String getField() {
		return field;
	}

	public Object getRejectedValue() {
		return rejectedValue;
	}
}
