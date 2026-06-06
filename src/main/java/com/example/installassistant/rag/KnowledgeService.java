package com.example.installassistant.rag;

import com.example.installassistant.model.KnowledgeDocument;
import com.example.installassistant.repository.KnowledgeDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 知识库管理服务 — 文档上传 / 删除 / 索引重建
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeService {

    private final DocumentLoader documentLoader;
    private final EmbeddingService embeddingService;
    private final KnowledgeDocumentRepository knowledgeDocumentRepository;

    /**
     * 上传文本内容到知识库
     */
    @Transactional
    public KnowledgeDocument uploadText(String text, String fileName, String docType) {
        log.info("[KnowledgeService] Uploading document: fileName={}, docType={}", fileName, docType);

        // 1. 分段
        List<Document> chunks = documentLoader.loadFromText(text, fileName, docType);

        // 2. 嵌入 + 存储
        embeddingService.embedAndStore(chunks);

        // 3. 记录元数据到 MySQL
        KnowledgeDocument kd = KnowledgeDocument.builder()
                .name(fileName)
                .docType(docType)
                .summary(text.length() > 500 ? text.substring(0, 500) + "..." : text)
                .chunkCount(chunks.size())
                .build();
        KnowledgeDocument saved = knowledgeDocumentRepository.save(kd);

        log.info("[KnowledgeService] Document uploaded: id={}, chunks={}", saved.getId(), chunks.size());
        return saved;
    }

    /**
     * 删除知识文档
     */
    @Transactional
    public void delete(Long id) {
        KnowledgeDocument kd = knowledgeDocumentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Knowledge document not found: " + id));

        // 从 Milvus 删除向量
        embeddingService.deleteByFilter(kd.getName());

        // 从 MySQL 删除元数据
        knowledgeDocumentRepository.delete(kd);

        log.info("[KnowledgeService] Document deleted: id={}, name={}", id, kd.getName());
    }

    /**
     * 列出所有知识文档
     */
    public List<KnowledgeDocument> listAll() {
        return knowledgeDocumentRepository.findAll();
    }
}
