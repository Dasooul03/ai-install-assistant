package com.example.installassistant.rag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 检索服务 — 语义检索相关文档
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RetrievalService {

    private final VectorStore vectorStore;

    private static final int TOP_K = 5;
    private static final double SIMILARITY_THRESHOLD = 0.5;

    /**
     * 根据用户 query 检索相关文档片段
     */
    public List<Document> retrieve(String query) {
        return retrieve(query, TOP_K);
    }

    /**
     * 根据用户 query 检索 topK 个相关文档
     */
    public List<Document> retrieve(String query, int topK) {
        long start = System.currentTimeMillis();
        log.info("[RetrievalService] Retrieving documents for query: '{}', topK={}", query, topK);

        List<Document> results = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(topK)
                        .similarityThreshold(SIMILARITY_THRESHOLD)
                        .build()
        );

        long elapsed = System.currentTimeMillis() - start;
        log.info("[RetrievalService] Found {} documents in {}ms", results.size(), elapsed);

        return results;
    }

    /**
     * 检索并格式化为文本上下文
     */
    public String retrieveAsContext(String query) {
        List<Document> docs = retrieve(query);
        if (docs.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < docs.size(); i++) {
            Document doc = docs.get(i);
            sb.append("【文档片段 ").append(i + 1).append("】");
            sb.append(" (来源: ").append(doc.getMetadata().getOrDefault("source", "未知")).append(")\n");
            sb.append(doc.getText()).append("\n\n");
        }
        return sb.toString();
    }
}
