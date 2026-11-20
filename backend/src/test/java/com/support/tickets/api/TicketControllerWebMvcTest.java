package com.support.tickets.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.support.tickets.api.controller.TicketController;
import com.support.tickets.api.dto.response.TicketDetailResponse;
import com.support.tickets.api.exception.ApiExceptionHandler;
import com.support.tickets.application.TicketService;
import com.support.tickets.domain.TicketPriority;
import com.support.tickets.domain.TicketStatus;
import com.support.tickets.domain.exception.InvalidRequestException;
import com.support.tickets.domain.exception.InvalidTransitionException;
import com.support.tickets.domain.exception.TicketNotFoundException;

/**
 * Slice tests: REST contract status codes and error JSON shape (T13).
 */
@WebMvcTest(controllers = TicketController.class)
@Import(ApiExceptionHandler.class)
class TicketControllerWebMvcTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private TicketService ticketService;

	@Test
	void createBlankTitleReturnsValidationErrorBody() throws Exception {
		mockMvc.perform(post("/api/tickets")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"title":"","description":"Valid description"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
				.andExpect(jsonPath("$.message").isNotEmpty())
				.andExpect(jsonPath("$.path").value("/api/tickets"))
				.andExpect(jsonPath("$.timestamp").exists())
				.andExpect(jsonPath("$.error").exists())
				.andExpect(jsonPath("$.details").isArray());

		verify(ticketService, never()).create(any());
	}

	@Test
	void createWithClientStatusReturnsValidationError() throws Exception {
		mockMvc.perform(post("/api/tickets")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"title":"T","description":"D","status":"CLOSED"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
				.andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Status")));

		verify(ticketService, never()).create(any());
	}

	@Test
	void createUnknownPriorityReturnsValidationError() throws Exception {
		mockMvc.perform(post("/api/tickets")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"title":"T","description":"D","priority":"CRITICAL"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

		verify(ticketService, never()).create(any());
	}

	@Test
	void createSuccessReturns201() throws Exception {
		UUID id = UUID.randomUUID();
		Instant now = Instant.parse("2026-01-15T10:00:00Z");
		when(ticketService.create(any())).thenReturn(new TicketDetailResponse(
				id,
				"Hello",
				"World",
				TicketPriority.MEDIUM,
				null,
				TicketStatus.OPEN,
				now,
				now,
				List.of()));

		mockMvc.perform(post("/api/tickets")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"title":"Hello","description":"World"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(id.toString()))
				.andExpect(jsonPath("$.status").value("OPEN"));
	}

	@Test
	void getUnknownIdReturnsNotFoundBody() throws Exception {
		UUID id = UUID.randomUUID();
		when(ticketService.getById(id)).thenThrow(new TicketNotFoundException(id));

		mockMvc.perform(get("/api/tickets/{id}", id))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.code").value("NOT_FOUND"))
				.andExpect(jsonPath("$.message").isNotEmpty())
				.andExpect(jsonPath("$.path").value("/api/tickets/" + id));
	}

	@Test
	void getMalformedUuidReturnsValidationError() throws Exception {
		mockMvc.perform(get("/api/tickets/{id}", "not-a-uuid"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
	}

	@Test
	void patchWithStatusReturnsValidationError() throws Exception {
		UUID id = UUID.randomUUID();

		// Include a valid updatable field so the status-forbidden rule is the failure under test
		// (status-only body also fails isPatchNonEmpty, and Bean Validation message order is undefined).
		mockMvc.perform(patch("/api/tickets/{id}", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"title":"Still updating","status":"CLOSED"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
				.andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Status")))
				.andExpect(jsonPath("$.details[?(@.field=='statusAbsent')].message").exists());

		verify(ticketService, never()).patch(any(), any());
	}

	@Test
	void patchEmptyBodyReturnsValidationError() throws Exception {
		UUID id = UUID.randomUUID();

		mockMvc.perform(patch("/api/tickets/{id}", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

		verify(ticketService, never()).patch(any(), any());
	}

	@Test
	void listUnknownStatusReturnsValidationError() throws Exception {
		when(ticketService.list(any(), eq("NOPE"), any(), any(), any()))
				.thenThrow(new InvalidRequestException("Unknown status: NOPE", "status", "NOPE"));

		mockMvc.perform(get("/api/tickets").param("status", "NOPE"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
				.andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Unknown status")));
	}

	@Test
	void transitionIllegalReturns422InvalidTransition() throws Exception {
		UUID id = UUID.randomUUID();
		when(ticketService.transition(eq(id), any()))
				.thenThrow(new InvalidTransitionException(TicketStatus.CLOSED, TicketStatus.OPEN));

		mockMvc.perform(post("/api/tickets/{id}/transition", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"status":"OPEN"}
								"""))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.status").value(422))
				.andExpect(jsonPath("$.code").value("INVALID_TRANSITION"))
				.andExpect(jsonPath("$.message").value("Cannot transition from CLOSED to OPEN"))
				.andExpect(jsonPath("$.path").value("/api/tickets/" + id + "/transition"))
				.andExpect(jsonPath("$.timestamp").exists())
				.andExpect(jsonPath("$.error").exists());
	}

	@Test
	void transitionUnknownStatusEnumReturnsValidationError() throws Exception {
		UUID id = UUID.randomUUID();

		mockMvc.perform(post("/api/tickets/{id}/transition", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"status":"REOPENED"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

		verify(ticketService, never()).transition(any(), any());
	}
}
