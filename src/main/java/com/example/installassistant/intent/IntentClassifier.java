package com.example.installassistant.intent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 意图分类器 — 使用 LLM few-shot prompt 进行意图识别与实体抽取
 */
@Slf4j
@Component
public class IntentClassifier {

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;
    private final String systemPrompt;

    public IntentClassifier(ChatModel chatModel, ObjectMapper objectMapper) {
        this.chatModel = chatModel;
        this.objectMapper = objectMapper;
        this.systemPrompt = loadPromptTemplate();
    }

    /**
     * 对用户输入进行意图分类
     */
    public IntentResult classify(String userInput) {
        long start = System.currentTimeMillis();
        log.info("[IntentClassifier] Classifying: '{}'", userInput);

        try {
            String fullPrompt = systemPrompt + "\n" + userInput;
            String response = chatModel.chat(fullPrompt);
            log.debug("[IntentClassifier] LLM raw response: {}", response);

            IntentResult result = parseResponse(response, userInput);
            long elapsed = System.currentTimeMillis() - start;
            log.info("[IntentClassifier] Result: type={}, confidence={}, elapsed={}ms",
                    result.type(), result.confidence(), elapsed);
            return result;

        } catch (Exception e) {
            log.error("[IntentClassifier] Classification failed, falling back to CHITCHAT", e);
            long elapsed = System.currentTimeMillis() - start;
            log.warn("[IntentClassifier] Fallback to CHITCHAT after {}ms", elapsed);
            return IntentResult.chitchat(userInput);
        }
    }

    /**
     * 解析 LLM 返回的 JSON
     */
    private IntentResult parseResponse(String response, String originalInput) {
        // 提取 JSON 块（可能被 ```json ... ``` 包裹）
        String json = extractJson(response);

        try {
            IntentResponse ir = objectMapper.readValue(json, IntentResponse.class);
            IntentType type;
            try {
                type = IntentType.valueOf(ir.type);
            } catch (IllegalArgumentException e) {
                log.warn("[IntentClassifier] Unknown intent type from LLM: '{}', falling back to CHITCHAT", ir.type);
                type = IntentType.CHITCHAT;
            }
            return new IntentResult(type, ir.confidence, ir.parameters, originalInput);
        } catch (JsonProcessingException e) {
            log.error("[IntentClassifier] Failed to parse LLM response as JSON: {}", json, e);
            return IntentResult.chitchat(originalInput);
        }
    }

    /**
     * 从 LLM 响应中提取 JSON
     */
    private String extractJson(String response) {
        String trimmed = response.trim();
        // 去掉 markdown 代码块标记
        if (trimmed.startsWith("```")) {
            int start = trimmed.indexOf("\n");
            int end = trimmed.lastIndexOf("```");
            if (start >= 0 && end > start) {
                return trimmed.substring(start + 1, end).trim();
            }
        }
        // 尝试直接找到第一个 { 到最后一个 }
        int braceStart = trimmed.indexOf("{");
        int braceEnd = trimmed.lastIndexOf("}");
        if (braceStart >= 0 && braceEnd > braceStart) {
            return trimmed.substring(braceStart, braceEnd + 1);
        }
        return trimmed;
    }

    /**
     * 加载 prompt 模板
     */
    private String loadPromptTemplate() {
        try (InputStream is = getClass().getClassLoader()
                .getResourceAsStream("prompts/intent-classifier.st")) {
            if (is == null) {
                log.error("[IntentClassifier] Template not found: prompts/intent-classifier.st");
                return getDefaultPrompt();
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("[IntentClassifier] Failed to load template", e);
            return getDefaultPrompt();
        }
    }

    private String getDefaultPrompt() {
        return """
                你是一个指令意图分类器。请分析用户输入,判断其意图类型并提取关键参数。
                意图类型: KNOWLEDGE_QA, INSTALL_GUIDE, CREATE_CLUSTER, CREATE_PARTITION, ADD_INSTANCE, SERVICE_LIFECYCLE, DIAGNOSTIC, CHITCHAT
                返回格式: {"type":"INTENT_TYPE","confidence":0.95,"parameters":{}}""";
    }

    /**
     * LLM 响应的 JSON 结构
     */
    private static class IntentResponse {
        public String type;
        public double confidence;
        public Map<String, String> parameters = Map.of();
    }
}
