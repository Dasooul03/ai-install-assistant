package com.example.installassistant.service;

import com.example.installassistant.agent.AgentRouter;
import com.example.installassistant.intent.IntentClassifier;
import com.example.installassistant.intent.IntentResult;
import com.example.installassistant.model.ConversationMessage;
import com.example.installassistant.model.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 聊天服务 — 全链路编排
 * 1. 会话管理
 * 2. 意图识别
 * 3. Agent 路由
 * 4. 消息持久化
 */
@Slf4j
@Service
public class ChatService {

    private final SessionService sessionService;
    private final ConversationHistoryService historyService;
    private final IntentClassifier intentClassifier;
    private final AgentRouter agentRouter;

    public ChatService(SessionService sessionService,
                       ConversationHistoryService historyService,
                       IntentClassifier intentClassifier,
                       AgentRouter agentRouter) {
        this.sessionService = sessionService;
        this.historyService = historyService;
        this.intentClassifier = intentClassifier;
        this.agentRouter = agentRouter;
    }

    /**
     * 处理用户消息（同步）
     */
    @Transactional
    public String processMessage(String userMessage, Long sessionId) {
        long start = System.currentTimeMillis();

        sessionId = ensureSession(sessionId);

        // 保存用户消息
        historyService.appendMessage(sessionId, "USER", userMessage);

        // 意图识别
        IntentResult intent = intentClassifier.classify(userMessage);
        log.info("[ChatService] Intent: type={}, confidence={}, sessionId={}",
                intent.type(), intent.confidence(), sessionId);

        // Agent 路由
        String response = agentRouter.route(userMessage, String.valueOf(sessionId));

        // 保存助手回复
        historyService.appendMessage(sessionId, "ASSISTANT", response);

        // 首次对话时更新标题
        try {
            List<ConversationMessage> msgs = historyService.getAllMessages(sessionId);
            if (msgs.size() <= 2) {
                String title = userMessage.length() > 20
                        ? userMessage.substring(0, 20) + "..."
                        : userMessage;
                sessionService.updateTitle(sessionId, title);
            }
        } catch (Exception e) {
            log.warn("[ChatService] Failed to update session title", e);
        }

        long elapsed = System.currentTimeMillis() - start;
        log.info("[ChatService] Processed in {}ms, sessionId={}, intent={}", elapsed, sessionId, intent.type());

        return response;
    }

    /**
     * 获取会话列表
     */
    public List<Session> listSessions() {
        return sessionService.listActive();
    }

    /**
     * 获取会话历史
     */
    public List<ConversationMessage> getSessionHistory(Long sessionId) {
        return historyService.getAllMessages(sessionId);
    }

    /**
     * 创建新会话
     */
    public Session createSession() {
        return sessionService.createSession();
    }

    private Long ensureSession(Long sessionId) {
        if (sessionId == null || sessionService.findById(sessionId).isEmpty()) {
            return sessionService.createSession().getId();
        }
        return sessionId;
    }
}
