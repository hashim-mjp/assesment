package com.support.tickets.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import jakarta.persistence.EntityManager;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Persistence proof (FR8 pattern 1 from {@code spec/test-strategy.md}):
 * create ticket + comment via API, clear the persistence context, reload via API —
 * data still present with the same field values.
 * <p>
 * Process-style restart with file H2 is covered manually under T15.
 */
@SpringBootTest
class TicketPersistenceIT {

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Autowired
	private EntityManager entityManager;

	private final ObjectMapper objectMapper = new ObjectMapper();

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	}

	@Test
	void ticketAndCommentSurvivePersistenceContextClear() throws Exception {
		String title = "Persist-" + UUID.randomUUID();
		MvcResult create = mockMvc.perform(post("/api/tickets")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"title":"%s","description":"Durable body","assignee":"sam"}
								""".formatted(title)))
				.andExpect(status().isCreated())
				.andReturn();

		JsonNode created = objectMapper.readTree(create.getResponse().getContentAsString());
		UUID id = UUID.fromString(created.get("id").asText());

		MvcResult commentResult = mockMvc.perform(post("/api/tickets/{id}/comments", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"body":"Persisted note","author":"alex"}
								"""))
				.andExpect(status().isCreated())
				.andReturn();
		String commentId = objectMapper.readTree(commentResult.getResponse().getContentAsString())
				.get("id")
				.asText();

		entityManager.clear();

		MvcResult reloaded = mockMvc.perform(get("/api/tickets/{id}", id))
				.andExpect(status().isOk())
				.andReturn();

		JsonNode detail = objectMapper.readTree(reloaded.getResponse().getContentAsString());
		assertThat(detail.get("id").asText()).isEqualTo(id.toString());
		assertThat(detail.get("title").asText()).isEqualTo(title);
		assertThat(detail.get("description").asText()).isEqualTo("Durable body");
		assertThat(detail.get("assignee").asText()).isEqualTo("sam");
		assertThat(detail.get("status").asText()).isEqualTo("OPEN");
		assertThat(detail.get("comments")).hasSize(1);
		assertThat(detail.get("comments").get(0).get("id").asText()).isEqualTo(commentId);
		assertThat(detail.get("comments").get(0).get("body").asText()).isEqualTo("Persisted note");
		assertThat(detail.get("comments").get(0).get("author").asText()).isEqualTo("alex");
	}
}
