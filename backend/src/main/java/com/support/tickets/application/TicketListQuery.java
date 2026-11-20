package com.support.tickets.application;

import java.util.Locale;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.support.tickets.domain.TicketStatus;
import com.support.tickets.domain.exception.InvalidRequestException;

/**
 * Parses and validates list endpoint query parameters ({@code q}, {@code status}, {@code page}, {@code size}, {@code sort}).
 */
final class TicketListQuery {

	private static final int DEFAULT_PAGE = 0;
	private static final int DEFAULT_SIZE = 20;
	private static final int MAX_SIZE = 100;
	private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "createdAt");
	private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("createdAt", "updatedAt");

	private final String keyword;
	private final TicketStatus status;
	private final Pageable pageable;

	private TicketListQuery(String keyword, TicketStatus status, Pageable pageable) {
		this.keyword = keyword;
		this.status = status;
		this.pageable = pageable;
	}

	static TicketListQuery parse(String q, String statusParam, Integer page, Integer size, String sortParam) {
		int pageIndex = page != null ? page : DEFAULT_PAGE;
		int pageSize = size != null ? size : DEFAULT_SIZE;

		if (pageIndex < 0) {
			throw new InvalidRequestException("page must be >= 0", "page", pageIndex);
		}
		if (pageSize < 1 || pageSize > MAX_SIZE) {
			throw new InvalidRequestException(
					"size must be between 1 and " + MAX_SIZE,
					"size",
					pageSize);
		}

		return new TicketListQuery(
				normalizeSearchQuery(q),
				parseStatusFilter(statusParam),
				PageRequest.of(pageIndex, pageSize, parseSort(sortParam)));
	}

	String keyword() {
		return keyword;
	}

	TicketStatus status() {
		return status;
	}

	Pageable pageable() {
		return pageable;
	}

	/** Empty / whitespace-only {@code q} means no search filter (api-contract §2). */
	private static String normalizeSearchQuery(String q) {
		if (q == null || q.isBlank()) {
			return null;
		}
		return q.trim();
	}

	private static TicketStatus parseStatusFilter(String statusParam) {
		if (statusParam == null || statusParam.isBlank()) {
			return null;
		}
		String normalized = statusParam.trim();
		try {
			return TicketStatus.valueOf(normalized);
		}
		catch (IllegalArgumentException ex) {
			throw new InvalidRequestException(
					"Unknown status: " + normalized,
					"status",
					normalized);
		}
	}

	/**
	 * Allowed: {@code createdAt,asc|desc}, {@code updatedAt,asc|desc}. Default {@code createdAt,desc}.
	 */
	private static Sort parseSort(String sortParam) {
		if (sortParam == null || sortParam.isBlank()) {
			return DEFAULT_SORT;
		}

		String[] parts = sortParam.split(",", 2);
		if (parts.length != 2) {
			throw new InvalidRequestException(
					"sort must be field,asc|desc (allowed fields: createdAt, updatedAt)",
					"sort",
					sortParam);
		}

		String field = parts[0].trim();
		String direction = parts[1].trim().toLowerCase(Locale.ROOT);

		if (!ALLOWED_SORT_FIELDS.contains(field)) {
			throw new InvalidRequestException(
					"sort field must be createdAt or updatedAt",
					"sort",
					sortParam);
		}
		if (!direction.equals("asc") && !direction.equals("desc")) {
			throw new InvalidRequestException(
					"sort direction must be asc or desc",
					"sort",
					sortParam);
		}

		return Sort.by(Sort.Direction.fromString(direction), field);
	}
}
