package com.example.installassistant.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * API 索引
 */
@RestController
@RequestMapping("/api")
public class IndexController {

    @GetMapping
    public Map<String, Object> index() {
        Map<String, String> endpoints = new LinkedHashMap<>();
        endpoints.put("chat (SSE)", "POST /api/chat");
        endpoints.put("chat (sync)", "POST /api/chat/sync");
        endpoints.put("api index", "GET /api");
        endpoints.put("sessions", "GET /api/sessions");
        endpoints.put("create session", "POST /api/sessions");
        endpoints.put("session history", "GET /api/sessions/{id}/history");
        endpoints.put("knowledge list", "GET /api/knowledge/list");
        endpoints.put("knowledge upload text", "POST /api/knowledge/upload/text");
        endpoints.put("knowledge upload file", "POST /api/knowledge/upload/file");
        endpoints.put("health", "GET /actuator/health");
        endpoints.put("MCP SSE", "GET /mcp/sse");

        return Map.of(
                "app", "智能安装助手 (AI Install Assistant)",
                "version", "0.1.0",
                "endpoints", endpoints
        );
    }
}
