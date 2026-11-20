package com.support.tickets.api.mapper;

import java.util.List;

import com.support.tickets.api.dto.response.CommentResponse;
import com.support.tickets.api.dto.response.TicketDetailResponse;
import com.support.tickets.api.dto.response.TicketSummaryResponse;
import com.support.tickets.domain.entity.Comment;
import com.support.tickets.domain.entity.Ticket;

public final class TicketMapper {

	private TicketMapper() {
	}

	public static TicketSummaryResponse toSummary(Ticket ticket) {
		return new TicketSummaryResponse(
				ticket.getId(),
				ticket.getTitle(),
				ticket.getPriority(),
				ticket.getAssignee(),
				ticket.getStatus(),
				ticket.getCreatedAt(),
				ticket.getUpdatedAt());
	}

	public static TicketDetailResponse toDetail(Ticket ticket, List<Comment> comments) {
		return new TicketDetailResponse(
				ticket.getId(),
				ticket.getTitle(),
				ticket.getDescription(),
				ticket.getPriority(),
				ticket.getAssignee(),
				ticket.getStatus(),
				ticket.getCreatedAt(),
				ticket.getUpdatedAt(),
				comments.stream().map(TicketMapper::toComment).toList());
	}

	public static CommentResponse toComment(Comment comment) {
		return new CommentResponse(
				comment.getId(),
				comment.getBody(),
				comment.getAuthor(),
				comment.getCreatedAt());
	}
}
