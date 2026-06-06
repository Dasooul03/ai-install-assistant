package com.example.installassistant.controller;

import com.example.installassistant.model.ConversationMessage;
import com.example.installassistant.model.Session;
import com.example.installassistant.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * 聊天入口控制器 — SSE 流式对话
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * 发送消息并获取 AI 回复（SSE 流式）
     *
     * Request body: { "message": "你好", "sessionId": 1 }
     */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@RequestBody ChatRequest request) {
        log.info("[ChatController] Chat request: message='{}', sessionId={}",
                request.message(), request.sessionId());

        // 同步处理（Phase 5 可升级为 StreamingChatModel 流式）
        String response = chatService.processMessage(request.message(), request.sessionId());

        // 以 SSE 格式逐字符流式返回
        return Flux.fromStream(response.chars().mapToObj(c -> String.valueOf((char) c)))
                .delayElements(Duration.ofMillis(30))
                .startWith(chatService.listSessions().stream()
                        .filter(s -> s.getId().equals(request.sessionId()))
                        .findFirst()
                        .map(s -> "event: session\ndata: " + s.getId() + "\n\n")
                        .orElse("event: session\ndata: 0\n\n"))
                .concatWithValues("\n\n[DONE]");
    }

    /**
     * 同步聊天（非流式，方便调试）
     */
    @PostMapping(value = "/chat/sync", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> chatSync(@RequestBody ChatRequest request) {
        log.info("[ChatController] Sync chat: message='{}'", request.message());
        long start = System.currentTimeMillis();

        String response = chatService.processMessage(request.message(), request.sessionId());

        long elapsed = System.currentTimeMillis() - start;
        return Map.of(
                "response", response,
                "elapsedMs", elapsed
        );
    }

    /**
     * 获取活跃会话列表
     */
    @GetMapping("/sessions")
    public List<Session> listSessions() {
        log.info("[ChatController] Listing sessions");
        return chatService.listSessions();
    }

    /**
     * 获取指定会话的历史消息
     */
    @GetMapping("/sessions/{id}/history")
    public List<ConversationMessage> getHistory(@PathVariable Long id) {
        log.info("[ChatController] Getting history for session {}", id);
        return chatService.getSessionHistory(id);
    }

    /**
     * 创建新会话
     */
    @PostMapping("/sessions")
    public Session createSession() {
        log.info("[ChatController] Creating new session");
        return chatService.createSession();
    }

    // ===== DTOs =====

    public record ChatRequest(String message, Long sessionId) {}
}
