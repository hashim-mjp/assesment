package com.support.tickets.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.support.tickets.domain.TicketPriority;
import com.support.tickets.domain.TicketStatus;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Create-ticket body. {@code status} must not be supplied by the client
 * (always created as {@code OPEN}); any presence — including {@code null} — is rejected.
 */
public class CreateTicketRequest {

	private String title;
	private String description;
	private TicketPriority priority;
	private String assignee;
	private TicketStatus status;
	private boolean statusPresent;

	@NotBlank
	@Size(max = 200)
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@NotBlank
	@Size(max = 10_000)
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public TicketPriority getPriority() {
		return priority;
	}

	public void setPriority(TicketPriority priority) {
		this.priority = priority;
	}

	@Size(max = 120)
	public String getAssignee() {
		return assignee;
	}

	public void setAssignee(String assignee) {
		this.assignee = assignee;
	}

	public TicketStatus getStatus() {
		return status;
	}

	@JsonProperty("status")
	public void setStatus(TicketStatus status) {
		this.status = status;
		this.statusPresent = true;
	}

	public boolean isStatusPresent() {
		return statusPresent;
	}

	@AssertTrue(message = "Status must not be set on create; tickets are always created as OPEN")
	public boolean isStatusAbsent() {
		return !statusPresent;
	}
}
