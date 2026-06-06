package com.example.installassistant.service;

import com.example.installassistant.model.ConversationMessage;
import com.example.installassistant.repository.ConversationMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * 会话历史管理服务 — 支持长上下文跟踪
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationHistoryService {

    private final ConversationMessageRepository messageRepository;

    /**
     * 追加一条消息到会话历史
     */
    @Transactional
    public ConversationMessage appendMessage(Long sessionId, String role, String content) {
        ConversationMessage msg = ConversationMessage.builder()
                .sessionId(sessionId)
                .role(role)
                .content(content)
                .build();
        ConversationMessage saved = messageRepository.save(msg);
        log.debug("[ConversationHistory] Appended {} message to session {}", role, sessionId);
        return saved;
    }

    /**
     * 获取会话最近 N 条消息（正序）
     */
    public List<ConversationMessage> getRecentMessages(Long sessionId, int n) {
        List<ConversationMessage> messages = messageRepository
                .findBySessionIdOrderByCreatedAtDesc(sessionId, PageRequest.of(0, n));
        Collections.reverse(messages);
        return messages;
    }

    /**
     * 获取会话全部消息（正序）
     */
    public List<ConversationMessage> getAllMessages(Long sessionId) {
        return messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    /**
     * 构建聊天历史上下文文本（注入 LLM prompt）
     */
    public String buildChatHistoryContext(Long sessionId, int recentN) {
        List<ConversationMessage> messages = getRecentMessages(sessionId, recentN);
        if (messages.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (ConversationMessage msg : messages) {
            sb.append(msg.getRole()).append(": ").append(msg.getContent()).append("\n");
        }
        return sb.toString();
    }
}
