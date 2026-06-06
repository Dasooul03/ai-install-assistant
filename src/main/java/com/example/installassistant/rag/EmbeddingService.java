package com.example.installassistant.rag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 嵌入服务 — 将文档向量化并存入 Milvus
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final VectorStore vectorStore;

    /**
     * 批量嵌入并存储文档到向量数据库
     */
    public void embedAndStore(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            log.warn("[EmbeddingService] Empty document list, skipping");
            return;
        }

        long start = System.currentTimeMillis();
        log.info("[EmbeddingService] Starting embedding for {} documents", documents.size());

        try {
            vectorStore.add(documents);
            long elapsed = System.currentTimeMillis() - start;
            log.info("[EmbeddingService] Successfully embedded {} documents in {}ms", documents.size(), elapsed);
        } catch (Exception e) {
            log.error("[EmbeddingService] Failed to embed documents", e);
            throw new RuntimeException("Embedding failed", e);
        }
    }

    /**
     * 删除指定文档的所有向量
     */
    public void deleteByFilter(String sourceFileName) {
        log.info("[EmbeddingService] Deleting vectors for source: {}", sourceFileName);
        try {
            vectorStore.delete("source == '" + sourceFileName + "'");
            log.info("[EmbeddingService] Deleted vectors for source: {}", sourceFileName);
        } catch (Exception e) {
            log.error("[EmbeddingService] Failed to delete vectors for source: {}", sourceFileName, e);
        }
    }
}
