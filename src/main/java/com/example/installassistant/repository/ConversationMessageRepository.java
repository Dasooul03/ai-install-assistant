package com.example.installassistant.repository;

import com.example.installassistant.model.ConversationMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationMessageRepository extends JpaRepository<ConversationMessage, Long> {

    List<ConversationMessage> findBySessionIdOrderByCreatedAtAsc(Long sessionId);

    List<ConversationMessage> findBySessionIdOrderByCreatedAtDesc(Long sessionId, Pageable pageable);
}
