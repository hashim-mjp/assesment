package com.support.tickets.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.support.tickets.domain.TicketPriority;
import com.support.tickets.domain.TicketStatus;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;

/**
 * Partial update body. Field presence is tracked so omitted fields stay unchanged
 * while {@code assignee: null} can unassign. Bean Validation rejects empty bodies,
 * blank title/description when present, null priority when present, and any
 * {@code status} field (transitions use a separate endpoint).
 */
public class PatchTicketRequest {

	private String title;
	private boolean titlePresent;

	private String description;
	private boolean descriptionPresent;

	private TicketPriority priority;
	private boolean priorityPresent;

	private String assignee;
	private boolean assigneePresent;

	private TicketStatus status;
	private boolean statusPresent;

	@JsonProperty("title")
	public void setTitle(String title) {
		this.title = title;
		this.titlePresent = true;
	}

	@Size(max = 200)
	public String getTitle() {
		return title;
	}

	public boolean isTitlePresent() {
		return titlePresent;
	}

	@JsonProperty("description")
	public void setDescription(String description) {
		this.description = description;
		this.descriptionPresent = true;
	}

	@Size(max = 10_000)
	public String getDescription() {
		return description;
	}

	public boolean isDescriptionPresent() {
		return descriptionPresent;
	}

	@JsonProperty("priority")
	public void setPriority(TicketPriority priority) {
		this.priority = priority;
		this.priorityPresent = true;
	}

	public TicketPriority getPriority() {
		return priority;
	}

	public boolean isPriorityPresent() {
		return priorityPresent;
	}

	@JsonProperty("assignee")
	public void setAssignee(String assignee) {
		this.assignee = assignee;
		this.assigneePresent = true;
	}

	@Size(max = 120)
	public String getAssignee() {
		return assignee;
	}

	public boolean isAssigneePresent() {
		return assigneePresent;
	}

	@JsonProperty("status")
	public void setStatus(TicketStatus status) {
		this.status = status;
		this.statusPresent = true;
	}

	public TicketStatus getStatus() {
		return status;
	}

	public boolean isStatusPresent() {
		return statusPresent;
	}

	public boolean hasAnyUpdatableField() {
		return titlePresent || descriptionPresent || priorityPresent || assigneePresent;
	}

	@AssertTrue(message = "At least one of title, description, priority, or assignee must be provided")
	public boolean isPatchNonEmpty() {
		return hasAnyUpdatableField();
	}

	@AssertTrue(message = "Status must not be set via PATCH; use POST /api/tickets/{id}/transition")
	public boolean isStatusAbsent() {
		return !statusPresent;
	}

	@AssertTrue(message = "Title must not be blank")
	public boolean isTitleValidWhenPresent() {
		return !titlePresent || (title != null && !title.isBlank());
	}

	@AssertTrue(message = "Description must not be blank")
	public boolean isDescriptionValidWhenPresent() {
		return !descriptionPresent || (description != null && !description.isBlank());
	}

	@AssertTrue(message = "Priority must not be null")
	public boolean isPriorityValidWhenPresent() {
		return !priorityPresent || priority != null;
	}
}
