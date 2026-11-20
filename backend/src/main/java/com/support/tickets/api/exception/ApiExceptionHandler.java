package com.support.tickets.api.exception;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.support.tickets.api.dto.response.ErrorDetail;
import com.support.tickets.api.dto.response.ErrorResponse;
import com.support.tickets.domain.exception.InvalidRequestException;
import com.support.tickets.domain.exception.InvalidTransitionException;
import com.support.tickets.domain.exception.TicketNotFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.exc.InvalidFormatException;
import tools.jackson.databind.exc.MismatchedInputException;

/**
 * Maps domain/application and framework exceptions to the stable error JSON
 * defined in {@code spec/api-contract.md} / {@code rules/api-standards.md}.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

	@ExceptionHandler(InvalidTransitionException.class)
	public ResponseEntity<ErrorResponse> handleInvalidTransition(
			InvalidTransitionException ex,
			WebRequest request) {
		HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
		ErrorResponse body = new ErrorResponse(
				Instant.now(),
				status.value(),
				status.getReasonPhrase(),
				"INVALID_TRANSITION",
				ex.getMessage(),
				pathOf(request),
				List.of(new ErrorDetail("status", "transition not allowed", ex.getTo().name())));
		return ResponseEntity.status(status).body(body);
	}

	@ExceptionHandler(TicketNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleNotFound(
			TicketNotFoundException ex,
			WebRequest request) {
		HttpStatus status = HttpStatus.NOT_FOUND;
		ErrorResponse body = new ErrorResponse(
				Instant.now(),
				status.value(),
				status.getReasonPhrase(),
				"NOT_FOUND",
				ex.getMessage(),
				pathOf(request),
				List.of());
		return ResponseEntity.status(status).body(body);
	}

	@ExceptionHandler(InvalidRequestException.class)
	public ResponseEntity<ErrorResponse> handleInvalidRequest(
			InvalidRequestException ex,
			WebRequest request) {
		HttpStatus status = HttpStatus.BAD_REQUEST;
		List<ErrorDetail> details = ex.getField() != null
				? List.of(new ErrorDetail(ex.getField(), ex.getMessage(), ex.getRejectedValue()))
				: List.of();
		ErrorResponse body = new ErrorResponse(
				Instant.now(),
				status.value(),
				status.getReasonPhrase(),
				"VALIDATION_ERROR",
				ex.getMessage(),
				pathOf(request),
				details);
		return ResponseEntity.status(status).body(body);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
			MethodArgumentNotValidException ex,
			WebRequest request) {
		List<ErrorDetail> details = ex.getBindingResult().getAllErrors().stream()
				.map(ApiExceptionHandler::toDetail)
				.toList();
		return validationError(firstMessage(details, "Validation failed"), pathOf(request), details);
	}

	@ExceptionHandler(BindException.class)
	public ResponseEntity<ErrorResponse> handleBindException(BindException ex, WebRequest request) {
		List<ErrorDetail> details = ex.getBindingResult().getAllErrors().stream()
				.map(ApiExceptionHandler::toDetail)
				.toList();
		return validationError(firstMessage(details, "Validation failed"), pathOf(request), details);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ErrorResponse> handleConstraintViolation(
			ConstraintViolationException ex,
			WebRequest request) {
		List<ErrorDetail> details = ex.getConstraintViolations().stream()
				.map(ApiExceptionHandler::toDetail)
				.toList();
		return validationError(firstMessage(details, "Validation failed"), pathOf(request), details);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleNotReadable(
			HttpMessageNotReadableException ex,
			WebRequest request) {
		String field = null;
		Object rejected = null;
		String message = "Malformed JSON request body";

		Throwable cause = ex.getMostSpecificCause();
		if (cause instanceof InvalidFormatException ife) {
			field = jsonPath(ife);
			rejected = ife.getValue();
			if (ife.getTargetType() != null && ife.getTargetType().isEnum()) {
				message = "Unknown value for " + (field != null ? field : "enum") + ": " + ife.getValue();
			}
			else {
				message = "Invalid value for " + (field != null ? field : "field");
			}
		}
		else if (cause instanceof MismatchedInputException mie) {
			field = jsonPath(mie);
			message = field != null ? "Invalid or missing value for " + field : message;
		}

		List<ErrorDetail> details = field != null
				? List.of(new ErrorDetail(field, message, rejected))
				: List.of();
		return validationError(message, pathOf(request), details);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErrorResponse> handleTypeMismatch(
			MethodArgumentTypeMismatchException ex,
			WebRequest request) {
		String field = ex.getName();
		Object rejected = ex.getValue();
		String message;
		if (ex.getRequiredType() != null && java.util.UUID.class.equals(ex.getRequiredType())) {
			message = "Malformed UUID: " + rejected;
		}
		else {
			message = "Invalid value for " + field + ": " + rejected;
		}
		List<ErrorDetail> details = List.of(new ErrorDetail(field, message, rejected));
		return validationError(message, pathOf(request), details);
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ErrorResponse> handleMissingParam(
			MissingServletRequestParameterException ex,
			WebRequest request) {
		String message = "Missing required parameter: " + ex.getParameterName();
		List<ErrorDetail> details = List.of(
				new ErrorDetail(ex.getParameterName(), message, null));
		return validationError(message, pathOf(request), details);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, WebRequest request) {
		log.error("Unhandled error on {}", pathOf(request), ex);
		HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
		ErrorResponse body = new ErrorResponse(
				Instant.now(),
				status.value(),
				status.getReasonPhrase(),
				"INTERNAL_ERROR",
				"An unexpected error occurred",
				pathOf(request),
				List.of());
		return ResponseEntity.status(status).body(body);
	}

	private static ResponseEntity<ErrorResponse> validationError(
			String message,
			String path,
			List<ErrorDetail> details) {
		HttpStatus status = HttpStatus.BAD_REQUEST;
		ErrorResponse body = new ErrorResponse(
				Instant.now(),
				status.value(),
				status.getReasonPhrase(),
				"VALIDATION_ERROR",
				message,
				path,
				details);
		return ResponseEntity.status(status).body(body);
	}

	private static ErrorDetail toDetail(ObjectError error) {
		if (error instanceof FieldError fieldError) {
			return new ErrorDetail(
					fieldError.getField(),
					fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "invalid",
					fieldError.getRejectedValue());
		}
		return new ErrorDetail(
				error.getObjectName(),
				error.getDefaultMessage() != null ? error.getDefaultMessage() : "invalid",
				null);
	}

	private static ErrorDetail toDetail(ConstraintViolation<?> violation) {
		String field = violation.getPropertyPath() != null
				? violation.getPropertyPath().toString()
				: null;
		return new ErrorDetail(field, violation.getMessage(), violation.getInvalidValue());
	}

	private static String firstMessage(List<ErrorDetail> details, String fallback) {
		if (details == null || details.isEmpty()) {
			return fallback;
		}
		ErrorDetail first = details.get(0);
		return first.message() != null ? first.message() : fallback;
	}

	private static String jsonPath(JacksonException ex) {
		if (ex.getPath() == null || ex.getPath().isEmpty()) {
			return null;
		}
		return ex.getPath().stream()
				.map(JacksonException.Reference::getPropertyName)
				.filter(name -> name != null && !name.isBlank())
				.collect(Collectors.joining("."));
	}

	private static String pathOf(WebRequest request) {
		if (request instanceof ServletWebRequest servletWebRequest) {
			HttpServletRequest http = servletWebRequest.getRequest();
			return http.getRequestURI();
		}
		return request.getDescription(false).replace("uri=", "");
	}
}
