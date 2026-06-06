package com.example.installassistant.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 文档加载器 — 支持 Markdown / TXT 文件的读取与分段
 */
@Slf4j
@Component
public class DocumentLoader {

    private static final int MAX_CHUNK_SIZE = 512; // 字符数
    private static final int OVERLAP_SIZE = 50;

    /**
     * 加载文件并分段
     */
    public List<Document> load(Path filePath) throws IOException {
        String fileName = filePath.getFileName().toString();
        String content = Files.readString(filePath);

        log.info("[DocumentLoader] Loading file: {}, size={} chars", fileName, content.length());

        String docType = detectDocType(fileName);
        List<String> chunks = splitContent(content, docType);
        List<Document> documents = new ArrayList<>();

        for (int i = 0; i < chunks.size(); i++) {
            Document doc = new Document(
                    chunks.get(i),
                    Map.of(
                            "source", fileName,
                            "docType", docType,
                            "chunkIndex", String.valueOf(i),
                            "totalChunks", String.valueOf(chunks.size())
                    )
            );
            documents.add(doc);
        }

        log.info("[DocumentLoader] File '{}' split into {} chunks", fileName, chunks.size());
        return documents;
    }

    /**
     * 加载文本字符串并分段（用于 API 上传）
     */
    public List<Document> loadFromText(String text, String fileName, String docType) {
        log.info("[DocumentLoader] Loading text: fileName={}, size={} chars", fileName, text.length());
        List<String> chunks = splitContent(text, docType);
        List<Document> documents = new ArrayList<>();

        for (int i = 0; i < chunks.size(); i++) {
            Document doc = new Document(
                    chunks.get(i),
                    Map.of(
                            "source", fileName,
                            "docType", docType,
                            "chunkIndex", String.valueOf(i),
                            "totalChunks", String.valueOf(chunks.size())
                    )
            );
            documents.add(doc);
        }

        log.info("[DocumentLoader] Text '{}' split into {} chunks", fileName, chunks.size());
        return documents;
    }

    /**
     * 内容分段策略：
     * 1. Markdown 按 ## 标题分段
     * 2. 超过 MAX_CHUNK_SIZE 的段按滑动窗口切分
     */
    private List<String> splitContent(String content, String docType) {
        List<String> result = new ArrayList<>();
        String[] sections;

        if (docType.equals("MANUAL") || docType.equals("FAQ")) {
            // 按 Markdown 标题分段
            sections = content.split("\n(?=## )");
        } else {
            // 按段落分段
            sections = content.split("\n\n+");
        }

        for (String section : sections) {
            String trimmed = section.trim();
            if (trimmed.isEmpty()) continue;

            if (trimmed.length() <= MAX_CHUNK_SIZE) {
                result.add(trimmed);
            } else {
                // 滑动窗口切分
                int start = 0;
                while (start < trimmed.length()) {
                    int end = Math.min(start + MAX_CHUNK_SIZE, trimmed.length());
                    String chunk = trimmed.substring(start, end);
                    result.add(chunk);
                    start = end - OVERLAP_SIZE;
                }
            }
        }

        return result;
    }

    private String detectDocType(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.contains("install") || lower.contains("安装") || lower.contains("manual")) return "MANUAL";
        if (lower.contains("faq") || lower.contains("常见") || lower.contains("qa")) return "FAQ";
        if (lower.contains("config") || lower.contains("配置")) return "CONFIG";
        return "OTHER";
    }
}
