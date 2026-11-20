package com.support.tickets.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.support.tickets.domain.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

	List<Comment> findByTicket_IdOrderByCreatedAtAsc(UUID ticketId);
}
