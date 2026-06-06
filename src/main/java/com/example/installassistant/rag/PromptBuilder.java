package com.example.installassistant.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Prompt 构造器 — 加载模板并填充检索结果
 */
@Slf4j
@Component
public class PromptBuilder {

    private static final String QA_TEMPLATE_PATH = "prompts/qa-system.st";

    /**
     * 构建带 RAG 上下文的系统提示词
     */
    public String buildSystemPrompt(String retrievalContext) {
        String template = loadTemplate(QA_TEMPLATE_PATH);
        if (template == null) {
            // Fallback 模板
            template = """
                    你是一个智能安装助手。请使用以下参考文档来回答用户问题。
                    如果参考文档中没有相关信息，请如实告知用户。
                    
                    参考文档：
                    {context}
                    
                    请用中文回答。
                    """;
        }
        return template.replace("{context}", retrievalContext);
    }

    /**
     * 加载 classpath 下的模板文件
     */
    private String loadTemplate(String path) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                log.warn("[PromptBuilder] Template not found: {}", path);
                return null;
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("[PromptBuilder] Failed to load template: {}", path, e);
            return null;
        }
    }
}
