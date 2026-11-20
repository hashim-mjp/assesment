package com.support.tickets.domain.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * Append-only comment on a ticket. No {@code updated_at} — edits are out of scope.
 */
@Entity
@Table(name = "comment")
public class Comment {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(nullable = false, updatable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "ticket_id", nullable = false, updatable = false)
	private Ticket ticket;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String body;

	@Column(length = 120)
	private String author;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected Comment() {
		// JPA
	}

	public Comment(Ticket ticket, String body, String author) {
		this.ticket = ticket;
		this.body = body;
		this.author = author;
	}

	@PrePersist
	void onCreate() {
		createdAt = Instant.now();
	}

	public UUID getId() {
		return id;
	}

	public Ticket getTicket() {
		return ticket;
	}

	public String getBody() {
		return body;
	}

	public String getAuthor() {
		return author;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
