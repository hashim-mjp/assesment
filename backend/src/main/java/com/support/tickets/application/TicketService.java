package com.support.tickets.application;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.support.tickets.api.dto.request.AddCommentRequest;
import com.support.tickets.api.dto.request.CreateTicketRequest;
import com.support.tickets.api.dto.request.PatchTicketRequest;
import com.support.tickets.api.dto.request.TransitionTicketRequest;
import com.support.tickets.api.dto.response.CommentResponse;
import com.support.tickets.api.dto.response.PagedTicketsResponse;
import com.support.tickets.api.dto.response.TicketDetailResponse;
import com.support.tickets.api.mapper.TicketMapper;
import com.support.tickets.domain.TicketPriority;
import com.support.tickets.domain.TicketStatus;
import com.support.tickets.domain.TicketStatusTransitions;
import com.support.tickets.domain.entity.Comment;
import com.support.tickets.domain.entity.Ticket;
import com.support.tickets.domain.exception.TicketNotFoundException;
import com.support.tickets.persistence.CommentRepository;
import com.support.tickets.persistence.TicketRepository;
import com.support.tickets.persistence.TicketSpecifications;

@Service
public class TicketService {

	private final TicketRepository ticketRepository;
	private final CommentRepository commentRepository;

	public TicketService(TicketRepository ticketRepository, CommentRepository commentRepository) {
		this.ticketRepository = ticketRepository;
		this.commentRepository = commentRepository;
	}

	@Transactional
	public TicketDetailResponse create(CreateTicketRequest request) {
		// status presence / field limits enforced by Bean Validation on CreateTicketRequest
		TicketPriority priority = request.getPriority() != null ? request.getPriority() : TicketPriority.MEDIUM;
		String assignee = normalizeOptionalString(request.getAssignee());

		Ticket ticket = new Ticket(
				request.getTitle().trim(),
				request.getDescription().trim(),
				priority,
				assignee,
				TicketStatus.OPEN);

		Ticket saved = ticketRepository.save(ticket);
		return TicketMapper.toDetail(saved, List.of());
	}

	@Transactional(readOnly = true)
	public PagedTicketsResponse list(String q, String statusParam, Integer page, Integer size, String sortParam) {
		TicketListQuery query = TicketListQuery.parse(q, statusParam, page, size, sortParam);

		Specification<Ticket> spec = TicketSpecifications.withFilters(query.keyword(), query.status());
		Page<Ticket> result = ticketRepository.findAll(spec, query.pageable());

		return new PagedTicketsResponse(
				result.getContent().stream().map(TicketMapper::toSummary).toList(),
				result.getNumber(),
				result.getSize(),
				result.getTotalElements(),
				result.getTotalPages());
	}

	@Transactional(readOnly = true)
	public TicketDetailResponse getById(UUID id) {
		Ticket ticket = requireTicket(id);
		return TicketMapper.toDetail(ticket, commentsFor(id));
	}

	@Transactional
	public TicketDetailResponse patch(UUID id, PatchTicketRequest request) {
		// empty body, status-in-body, and field limits enforced by Bean Validation on PatchTicketRequest
		Ticket ticket = requireTicket(id);

		if (request.isTitlePresent()) {
			ticket.setTitle(request.getTitle().trim());
		}
		if (request.isDescriptionPresent()) {
			ticket.setDescription(request.getDescription().trim());
		}
		if (request.isPriorityPresent()) {
			ticket.setPriority(request.getPriority());
		}
		if (request.isAssigneePresent()) {
			ticket.setAssignee(normalizeOptionalString(request.getAssignee()));
		}

		Ticket saved = ticketRepository.save(ticket);
		return TicketMapper.toDetail(saved, commentsFor(id));
	}

	@Transactional
	public TicketDetailResponse transition(UUID id, TransitionTicketRequest request) {
		Ticket ticket = requireTicket(id);

		TicketStatus target = request.status();
		TicketStatusTransitions.assertAllowed(ticket.getStatus(), target);
		ticket.setStatus(target);

		Ticket saved = ticketRepository.save(ticket);
		return TicketMapper.toDetail(saved, commentsFor(id));
	}

	@Transactional
	public CommentResponse addComment(UUID ticketId, AddCommentRequest request) {
		Ticket ticket = requireTicket(ticketId);

		Comment comment = new Comment(
				ticket,
				request.body().trim(),
				normalizeOptionalString(request.author()));

		Comment saved = commentRepository.save(comment);
		return TicketMapper.toComment(saved);
	}

	private Ticket requireTicket(UUID id) {
		return ticketRepository.findById(id)
				.orElseThrow(() -> new TicketNotFoundException(id));
	}

	private List<Comment> commentsFor(UUID ticketId) {
		return commentRepository.findByTicket_IdOrderByCreatedAtAsc(ticketId);
	}

	private static String normalizeOptionalString(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}
}
