package com.support.tickets.domain;

import com.support.tickets.domain.exception.InvalidTransitionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Unit coverage of {@link TicketStatusTransitions} against {@code spec/state-machine.md}.
 * Does not replace HTTP integration matrix tests.
 */
class TicketStatusTransitionTest {

	private static final Set<String> ALLOWED_EDGES = Set.of(
			"OPEN->IN_PROGRESS",
			"OPEN->CANCELLED",
			"IN_PROGRESS->RESOLVED",
			"IN_PROGRESS->CANCELLED",
			"RESOLVED->CLOSED");

	@ParameterizedTest(name = "allows {0} → {1}")
	@MethodSource("allowedTransitions")
	void allowsTransition(TicketStatus from, TicketStatus to) {
		assertThat(TicketStatusTransitions.isAllowed(from, to)).isTrue();
		TicketStatusTransitions.assertAllowed(from, to);
	}

	@ParameterizedTest(name = "denies {0} → {1}")
	@MethodSource("deniedTransitions")
	void deniesTransition(TicketStatus from, TicketStatus to) {
		assertThat(TicketStatusTransitions.isAllowed(from, to)).isFalse();
		assertThatThrownBy(() -> TicketStatusTransitions.assertAllowed(from, to))
				.isInstanceOf(InvalidTransitionException.class)
				.hasMessageContaining("Cannot transition from " + from + " to " + to);
	}

	@Test
	void rejectsClosedToOpen() {
		assertThat(TicketStatusTransitions.isAllowed(TicketStatus.CLOSED, TicketStatus.OPEN)).isFalse();
	}

	@Test
	void rejectsResolvedToOpen() {
		assertThat(TicketStatusTransitions.isAllowed(TicketStatus.RESOLVED, TicketStatus.OPEN)).isFalse();
	}

	@Test
	void rejectsCancelledToOpen() {
		assertThat(TicketStatusTransitions.isAllowed(TicketStatus.CANCELLED, TicketStatus.OPEN)).isFalse();
	}

	@Test
	void nullStatusesAreDenied() {
		assertThat(TicketStatusTransitions.isAllowed(null, TicketStatus.OPEN)).isFalse();
		assertThat(TicketStatusTransitions.isAllowed(TicketStatus.OPEN, null)).isFalse();
	}

	@Test
	void matrixHasExactlyFiveAllows() {
		assertThat(ALLOWED_EDGES).hasSize(5);
		long allowCount = Stream.of(TicketStatus.values())
				.flatMap(from -> Stream.of(TicketStatus.values())
						.filter(to -> TicketStatusTransitions.isAllowed(from, to)))
				.count();
		assertThat(allowCount).isEqualTo(5);
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
}
