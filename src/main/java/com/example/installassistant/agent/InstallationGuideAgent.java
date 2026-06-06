package com.example.installassistant.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 安装指引智能体 — 分步指导用户完成安装
 */
@Slf4j
@Component
public class InstallationGuideAgent {

    public String guide(String query, String sessionId) {
        log.info("[InstallationGuideAgent] Processing guide request, sessionId={}", sessionId);
        // TODO: Phase 5 实现
        return "Installation guide placeholder";
    }
}
