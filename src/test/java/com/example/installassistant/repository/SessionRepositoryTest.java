package com.example.installassistant.repository;

import com.example.installassistant.model.ConversationMessage;
import com.example.installassistant.model.Session;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class SessionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SessionRepository sessionRepository;

    @Test
    void shouldCreateSession() {
        Session session = Session.builder().status("ACTIVE").title("测试会话").build();
        Session saved = sessionRepository.save(session);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void shouldFindActiveSessions() {
        sessionRepository.save(Session.builder().status("ACTIVE").title("Active 1").build());
        sessionRepository.save(Session.builder().status("ACTIVE").title("Active 2").build());
        sessionRepository.save(Session.builder().status("CLOSED").title("Closed 1").build());

        List<Session> active = sessionRepository.findByStatusOrderByUpdatedAtDesc("ACTIVE");
        assertThat(active).hasSize(2);
    }
}
