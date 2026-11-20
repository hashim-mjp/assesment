package com.support.tickets.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.support.tickets.domain.TicketStatus;

import org.junit.jupiter.api.BeforeEach;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Full 5×5 state-machine matrix against {@code POST /api/tickets/{id}/transition}.
 * Authority: {@code spec/state-machine.md}. Backend rejection is proven via HTTP 422.
 */
@SpringBootTest
class TicketStateMachineIT {

	private static final Set<String> ALLOWED_EDGES = Set.of(
			"OPEN->IN_PROGRESS",
			"OPEN->CANCELLED",
			"IN_PROGRESS->RESOLVED",
			"IN_PROGRESS->CANCELLED",
			"RESOLVED->CLOSED");

	@Autowired
	private WebApplicationContext webApplicationContext;

	private final ObjectMapper objectMapper = new ObjectMapper();

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	}

	@ParameterizedTest(name = "allows {0} → {1}")
	@MethodSource("allowedTransitions")
	void allowsTransition(TicketStatus from, TicketStatus to) throws Exception {
		UUID id = ticketInStatus(from);

		mockMvc.perform(post("/api/tickets/{id}/transition", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"status\":\"" + to.name() + "\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value(to.name()));

		assertThat(currentStatus(id)).isEqualTo(to);
	}

	@ParameterizedTest(name = "denies {0} → {1}")
	@MethodSource("deniedTransitions")
	void deniesTransition(TicketStatus from, TicketStatus to) throws Exception {
		UUID id = ticketInStatus(from);

		mockMvc.perform(post("/api/tickets/{id}/transition", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"status\":\"" + to.name() + "\"}"))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.status").value(422))
				.andExpect(jsonPath("$.code").value("INVALID_TRANSITION"))
				.andExpect(jsonPath("$.message").isNotEmpty())
				.andExpect(jsonPath("$.path").value("/api/tickets/" + id + "/transition"))
				.andExpect(jsonPath("$.error").exists())
				.andExpect(jsonPath("$.timestamp").exists());

		assertThat(currentStatus(id)).isEqualTo(from);
	}

	@Test
	void rejectsClosedToOpen() throws Exception {
		UUID id = ticketInStatus(TicketStatus.CLOSED);

		mockMvc.perform(post("/api/tickets/{id}/transition", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"status\":\"OPEN\"}"))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.code").value("INVALID_TRANSITION"))
				.andExpect(jsonPath("$.message").value("Cannot transition from CLOSED to OPEN"));

		assertThat(currentStatus(id)).isEqualTo(TicketStatus.CLOSED);
	}

	@Test
	void rejectsResolvedToOpen() throws Exception {
		UUID id = ticketInStatus(TicketStatus.RESOLVED);

		mockMvc.perform(post("/api/tickets/{id}/transition", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"status\":\"OPEN\"}"))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.code").value("INVALID_TRANSITION"))
				.andExpect(jsonPath("$.message").value("Cannot transition from RESOLVED to OPEN"));

		assertThat(currentStatus(id)).isEqualTo(TicketStatus.RESOLVED);
	}

	@Test
	void rejectsCancelledToOpen() throws Exception {
		UUID id = ticketInStatus(TicketStatus.CANCELLED);

		mockMvc.perform(post("/api/tickets/{id}/transition", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"status\":\"OPEN\"}"))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.code").value("INVALID_TRANSITION"))
				.andExpect(jsonPath("$.message").value("Cannot transition from CANCELLED to OPEN"));

		assertThat(currentStatus(id)).isEqualTo(TicketStatus.CANCELLED);
	}

	@Test
	void happyPathOpenToInProgressToResolvedToClosed() throws Exception {
		UUID id = createTicket("Happy path chain");

		assertThat(currentStatus(id)).isEqualTo(TicketStatus.OPEN);
		transitionOk(id, TicketStatus.IN_PROGRESS);
		transitionOk(id, TicketStatus.RESOLVED);
		transitionOk(id, TicketStatus.CLOSED);
		assertThat(currentStatus(id)).isEqualTo(TicketStatus.CLOSED);
	}

	static Stream<Arguments> allowedTransitions() {
		return ALLOWED_EDGES.stream()
				.map(edge -> {
					String[] parts = edge.split("->");
					return Arguments.of(TicketStatus.valueOf(parts[0]), TicketStatus.valueOf(parts[1]));
				});
	}

	static Stream<Arguments> deniedTransitions() {
		List<Arguments> args = new ArrayList<>();
		for (TicketStatus from : TicketStatus.values()) {
			for (TicketStatus to : TicketStatus.values()) {
				String key = from.name() + "->" + to.name();
				if (!ALLOWED_EDGES.contains(key)) {
					args.add(Arguments.of(from, to));
				}
			}
		}
		assertThat(args).hasSize(20);
		return args.stream();
	}

	private UUID ticketInStatus(TicketStatus target) throws Exception {
		UUID id = createTicket("Seed " + target + " " + UUID.randomUUID());
		switch (target) {
			case OPEN -> {
				// create always yields OPEN
			}
			case IN_PROGRESS -> transitionOk(id, TicketStatus.IN_PROGRESS);
			case RESOLVED -> {
				transitionOk(id, TicketStatus.IN_PROGRESS);
				transitionOk(id, TicketStatus.RESOLVED);
			}
			case CLOSED -> {
				transitionOk(id, TicketStatus.IN_PROGRESS);
				transitionOk(id, TicketStatus.RESOLVED);
				transitionOk(id, TicketStatus.CLOSED);
			}
			case CANCELLED -> transitionOk(id, TicketStatus.CANCELLED);
		}
		assertThat(currentStatus(id)).isEqualTo(target);
		return id;
	}

	private UUID createTicket(String title) throws Exception {
		String body = """
				{"title":"%s","description":"State machine IT seed"}
				""".formatted(title.replace("\"", "'"));
		MvcResult result = mockMvc.perform(post("/api/tickets")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isCreated())
				.andReturn();
		JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
		return UUID.fromString(json.get("id").asText());
	}

	private void transitionOk(UUID id, TicketStatus to) throws Exception {
		mockMvc.perform(post("/api/tickets/{id}/transition", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"status\":\"" + to.name() + "\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value(to.name()));
	}

	private TicketStatus currentStatus(UUID id) throws Exception {
		MvcResult result = mockMvc.perform(get("/api/tickets/{id}", id))
				.andExpect(status().isOk())
				.andReturn();
		JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
		return TicketStatus.valueOf(json.get("status").asText());
	}
}
