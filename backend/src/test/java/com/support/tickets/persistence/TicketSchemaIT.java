package com.support.tickets.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import com.support.tickets.domain.entity.Comment;
import com.support.tickets.domain.entity.Ticket;
import com.support.tickets.domain.TicketPriority;
import com.support.tickets.domain.TicketStatus;

@SpringBootTest
class TicketSchemaIT {

	@Autowired
	private TicketRepository ticketRepository;

	@Autowired
	private CommentRepository commentRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void ticketTableHasStatusAndCreatedAtIndexes() {
		List<Map<String, Object>> indexes = jdbcTemplate.queryForList(
				"""
						SELECT INDEX_NAME
						FROM INFORMATION_SCHEMA.INDEXES
						WHERE TABLE_NAME = 'TICKET'
						  AND INDEX_NAME IN ('IDX_TICKET_STATUS', 'IDX_TICKET_CREATED_AT', 'IDX_TICKET_TITLE')
						""");

		assertThat(indexes)
				.extracting(row -> String.valueOf(row.get("INDEX_NAME")).toUpperCase())
				.contains("IDX_TICKET_STATUS", "IDX_TICKET_CREATED_AT", "IDX_TICKET_TITLE");
	}

	@Test
	void ticketPersistsAndReloads() {
		Ticket ticket = new Ticket(
				"Schema check",
				"Verify durable mapping",
				TicketPriority.MEDIUM,
				null,
				TicketStatus.OPEN);

		Ticket saved = ticketRepository.save(ticket);
		assertThat(saved.getId()).isNotNull();
		assertThat(saved.getCreatedAt()).isNotNull();
		assertThat(saved.getUpdatedAt()).isNotNull();

		Ticket reloaded = ticketRepository.findById(saved.getId()).orElseThrow();
		assertThat(reloaded.getTitle()).isEqualTo("Schema check");
		assertThat(reloaded.getStatus()).isEqualTo(TicketStatus.OPEN);
		assertThat(reloaded.getPriority()).isEqualTo(TicketPriority.MEDIUM);
	}

	@Test
	void commentTableHasTicketIdIndex() {
		List<Map<String, Object>> indexes = jdbcTemplate.queryForList(
				"""
						SELECT INDEX_NAME
						FROM INFORMATION_SCHEMA.INDEXES
						WHERE TABLE_NAME = 'COMMENT'
						  AND INDEX_NAME = 'IDX_COMMENT_TICKET_ID'
						""");

		assertThat(indexes)
				.extracting(row -> String.valueOf(row.get("INDEX_NAME")).toUpperCase())
				.contains("IDX_COMMENT_TICKET_ID");
	}

	@Test
	void commentPersistsAgainstTicketAndReloads() {
		Ticket ticket = ticketRepository.save(new Ticket(
				"Comment parent",
				"Parent for comment FK",
				TicketPriority.LOW,
				null,
				TicketStatus.OPEN));

		Comment saved = commentRepository.save(new Comment(ticket, "First note", "sam"));
		assertThat(saved.getId()).isNotNull();
		assertThat(saved.getCreatedAt()).isNotNull();

		Comment reloaded = commentRepository.findById(saved.getId()).orElseThrow();
		assertThat(reloaded.getBody()).isEqualTo("First note");
		assertThat(reloaded.getAuthor()).isEqualTo("sam");

		assertThat(commentRepository.findByTicket_IdOrderByCreatedAtAsc(ticket.getId()))
				.extracting(Comment::getId)
				.containsExactly(saved.getId());
	}
}
