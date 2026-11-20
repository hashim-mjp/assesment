package com.support.tickets.persistence;

import org.springframework.data.jpa.domain.Specification;

import com.support.tickets.domain.entity.Ticket;
import com.support.tickets.domain.TicketStatus;

import jakarta.persistence.criteria.Predicate;

/**
 * List filters for {@code GET /api/tickets}: optional keyword ({@code q}) and status, AND-combined.
 */
public final class TicketSpecifications {

	private TicketSpecifications() {
	}

	/**
	 * @param q      trimmed keyword, or {@code null} for no search filter
	 * @param status exact status, or {@code null} for all statuses
	 */
	public static Specification<Ticket> withFilters(String q, TicketStatus status) {
		return (root, query, cb) -> {
			Predicate predicate = cb.conjunction();

			if (status != null) {
				predicate = cb.and(predicate, cb.equal(root.get("status"), status));
			}

			if (q != null) {
				String pattern = "%" + q.toLowerCase() + "%";
				Predicate titleMatch = cb.like(cb.lower(root.get("title")), pattern);
				Predicate descriptionMatch = cb.like(cb.lower(root.get("description")), pattern);
				predicate = cb.and(predicate, cb.or(titleMatch, descriptionMatch));
			}

			return predicate;
		};
	}
}
