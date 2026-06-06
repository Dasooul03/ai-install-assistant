package com.example.installassistant.service;

import com.example.installassistant.model.Session;
import com.example.installassistant.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 会话管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;

    @Transactional
    public Session createSession() {
        Session session = Session.builder()
                .status("ACTIVE")
                .title("新会话")
                .build();
        Session saved = sessionRepository.save(session);
        log.info("[SessionService] Created session: id={}", saved.getId());
        return saved;
    }

    public Optional<Session> findById(Long id) {
        return sessionRepository.findById(id);
    }

    @Transactional
    public Session closeSession(Long id) {
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session not found: " + id));
        session.setStatus("CLOSED");
        Session saved = sessionRepository.save(session);
        log.info("[SessionService] Closed session: id={}", id);
        return saved;
    }

    public List<Session> listActive() {
        return sessionRepository.findByStatusOrderByUpdatedAtDesc("ACTIVE");
    }

    @Transactional
    public void updateTitle(Long id, String title) {
        sessionRepository.findById(id).ifPresent(session -> {
            session.setTitle(title);
            sessionRepository.save(session);
        });
    }
}
