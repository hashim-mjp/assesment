package com.support.tickets.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Integration proof for FR6/FR7: keyword search ({@code q}), status filter, and AND combine.
 */
@SpringBootTest
class TicketSearchFilterIT {

	@Autowired
	private WebApplicationContext webApplicationContext;

	private final ObjectMapper objectMapper = new ObjectMapper();

	private MockMvc mockMvc;

	private UUID titleMatchId;
	private UUID descriptionMatchId;
	private UUID closedOtherId;
	private UUID openNoiseId;

	@BeforeEach
	void setUp() throws Exception {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

		String suffix = UUID.randomUUID().toString().substring(0, 8);
		titleMatchId = createTicket("UniqueAlphaTitle-" + suffix, "generic body", null);
		descriptionMatchId = createTicket("Other title " + suffix, "UniqueBetaDesc-" + suffix, null);
		closedOtherId = createTicket("ClosedGamma-" + suffix, "not matching keyword", null);
		transition(closedOtherId, "IN_PROGRESS");
		transition(closedOtherId, "RESOLVED");
		transition(closedOtherId, "CLOSED");
		openNoiseId = createTicket("NoiseDelta-" + suffix, "noise only", null);
	}

	@Test
	void searchMatchesTitleCaseInsensitive() throws Exception {
		MvcResult result = mockMvc.perform(get("/api/tickets")
						.param("q", "uniquealphatitle")
						.param("size", "100"))
				.andExpect(status().isOk())
				.andReturn();

		List<String> ids = contentIds(result);
		assertThat(ids).contains(titleMatchId.toString());
		assertThat(ids).doesNotContain(descriptionMatchId.toString(), closedOtherId.toString(), openNoiseId.toString());
	}

	@Test
	void searchMatchesDescriptionNotComments() throws Exception {
		mockMvc.perform(post("/api/tickets/{id}/comments", openNoiseId)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"body":"UniqueBetaDesc-should-not-match-via-comment"}
								"""))
				.andExpect(status().isCreated());

		MvcResult result = mockMvc.perform(get("/api/tickets")
						.param("q", "UniqueBetaDesc")
						.param("size", "100"))
				.andExpect(status().isOk())
				.andReturn();

		List<String> ids = contentIds(result);
		assertThat(ids).contains(descriptionMatchId.toString());
		assertThat(ids).doesNotContain(openNoiseId.toString());
	}

	@Test
	void emptyOrWhitespaceQMeansNoSearchFilter() throws Exception {
		MvcResult blank = mockMvc.perform(get("/api/tickets").param("q", "   ").param("size", "100"))
				.andExpect(status().isOk())
				.andReturn();
		MvcResult omitted = mockMvc.perform(get("/api/tickets").param("size", "100"))
				.andExpect(status().isOk())
				.andReturn();

		assertThat(contentIds(blank)).contains(
				titleMatchId.toString(),
				descriptionMatchId.toString(),
				closedOtherId.toString(),
				openNoiseId.toString());
		assertThat(contentIds(blank)).containsExactlyInAnyOrderElementsOf(contentIds(omitted));
	}

	@Test
	void statusFilterReturnsOnlyMatchingStatus() throws Exception {
		MvcResult closed = mockMvc.perform(get("/api/tickets")
						.param("status", "CLOSED")
						.param("size", "100"))
				.andExpect(status().isOk())
				.andReturn();

		List<String> ids = contentIds(closed);
		assertThat(ids).contains(closedOtherId.toString());
		assertThat(ids).doesNotContain(
				titleMatchId.toString(),
				descriptionMatchId.toString(),
				openNoiseId.toString());

		JsonNode root = objectMapper.readTree(closed.getResponse().getContentAsString());
		for (JsonNode row : root.get("content")) {
			assertThat(row.get("status").asText()).isEqualTo("CLOSED");
		}
	}

	@Test
	void unknownStatusReturnsValidationError() throws Exception {
		mockMvc.perform(get("/api/tickets").param("status", "NOPE"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
				.andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Unknown status")));
	}

	@Test
	void combinedQAndStatusUsesAndSemantics() throws Exception {
		String suffix = UUID.randomUUID().toString().substring(0, 8);
		UUID openHit = createTicket("ComboSearch-" + suffix, "combo open body", null);
		UUID closedHit = createTicket("ComboSearch-" + suffix + "-closed", "combo closed body", null);
		transition(closedHit, "IN_PROGRESS");
		transition(closedHit, "RESOLVED");
		transition(closedHit, "CLOSED");

		MvcResult result = mockMvc.perform(get("/api/tickets")
						.param("q", "ComboSearch-" + suffix)
						.param("status", "OPEN")
						.param("size", "100"))
				.andExpect(status().isOk())
				.andReturn();

		List<String> ids = contentIds(result);
		assertThat(ids).contains(openHit.toString());
		assertThat(ids).doesNotContain(closedHit.toString());
	}

	private UUID createTicket(String title, String description, String assignee) throws Exception {
		String assigneeJson = assignee == null ? "" : ",\"assignee\":\"" + assignee + "\"";
		String body = """
				{"title":"%s","description":"%s"%s}
				""".formatted(title, description, assigneeJson);
		MvcResult result = mockMvc.perform(post("/api/tickets")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isCreated())
				.andReturn();
		return UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText());
	}

	private void transition(UUID id, String status) throws Exception {
		mockMvc.perform(post("/api/tickets/{id}/transition", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"status\":\"" + status + "\"}"))
				.andExpect(status().isOk());
	}

	private List<String> contentIds(MvcResult result) throws Exception {
		JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
		List<String> ids = new ArrayList<>();
		for (JsonNode row : root.get("content")) {
			ids.add(row.get("id").asText());
		}
		return ids;
	}
}
