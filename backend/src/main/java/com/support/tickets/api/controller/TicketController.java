package com.support.tickets.api.controller;

import java.net.URI;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.support.tickets.api.dto.request.AddCommentRequest;
import com.support.tickets.api.dto.request.CreateTicketRequest;
import com.support.tickets.api.dto.request.PatchTicketRequest;
import com.support.tickets.api.dto.request.TransitionTicketRequest;
import com.support.tickets.api.dto.response.CommentResponse;
import com.support.tickets.api.dto.response.PagedTicketsResponse;
import com.support.tickets.api.dto.response.TicketDetailResponse;
import com.support.tickets.application.TicketService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

	private final TicketService ticketService;

	public TicketController(TicketService ticketService) {
		this.ticketService = ticketService;
	}

	@PostMapping
	public ResponseEntity<TicketDetailResponse> create(@Valid @RequestBody CreateTicketRequest request) {
		TicketDetailResponse created = ticketService.create(request);
		URI location = UriComponentsBuilder
				.fromPath("/api/tickets/{id}")
				.buildAndExpand(created.id())
				.toUri();
		return ResponseEntity.created(location).body(created);
	}

	@GetMapping
	public PagedTicketsResponse list(
			@RequestParam(required = false) String q,
			@RequestParam(required = false) String status,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size,
			@RequestParam(required = false) String sort) {
		return ticketService.list(q, status, page, size, sort);
	}

	@GetMapping("/{id}")
	public TicketDetailResponse getById(@PathVariable UUID id) {
		return ticketService.getById(id);
	}

	@PatchMapping("/{id}")
	public TicketDetailResponse patch(
			@PathVariable UUID id,
			@Valid @RequestBody PatchTicketRequest request) {
		return ticketService.patch(id, request);
	}

	@PostMapping("/{id}/transition")
	public TicketDetailResponse transition(
			@PathVariable UUID id,
			@Valid @RequestBody TransitionTicketRequest request) {
		return ticketService.transition(id, request);
	}

	@PostMapping("/{id}/comments")
	public ResponseEntity<CommentResponse> addComment(
			@PathVariable UUID id,
			@Valid @RequestBody AddCommentRequest request) {
		CommentResponse created = ticketService.addComment(id, request);
		URI location = UriComponentsBuilder
				.fromPath("/api/tickets/{ticketId}/comments/{commentId}")
				.buildAndExpand(id, created.id())
				.toUri();
		return ResponseEntity.created(location).body(created);
	}
}
