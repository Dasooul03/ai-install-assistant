package com.example.installassistant.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 知识文档实体 — 记录已导入向量库的文档元数据
 */
@Entity
@Table(name = "knowledge_documents", indexes = {
        @Index(name = "idx_kdoc_type", columnList = "doc_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KnowledgeDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 文档名称 */
    @Column(nullable = false, length = 300)
    private String name;

    /** 文档类型: MANUAL / FAQ / CONFIG / OTHER */
    @Column(name = "doc_type", nullable = false, length = 20)
    private String docType;

    /** 文档内容摘要（前 500 字） */
    @Column(columnDefinition = "TEXT")
    private String summary;

    /** 分段数量 */
    @Column(name = "chunk_count")
    private Integer chunkCount;

    /** Milvus 中存储的向量 ID 列表 (JSON array) */
    @Column(name = "vector_ids", columnDefinition = "TEXT")
    private String vectorIds;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
