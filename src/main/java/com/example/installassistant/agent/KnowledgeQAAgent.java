package com.example.installassistant.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 知识问答智能体 — 处理安装知识、FAQ 类问题
 */
@Slf4j
@Component
public class KnowledgeQAAgent {

    public String answer(String query, String sessionId) {
        log.info("[KnowledgeQAAgent] Processing query, sessionId={}", sessionId);
        // TODO: Phase 5 实现 RAG 检索 + LLM 生成
        return "Knowledge QA placeholder";
    }
}
