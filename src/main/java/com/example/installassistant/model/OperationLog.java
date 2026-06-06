package com.example.installassistant.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 操作日志实体 — 记录每次自动化操作
 */
@Entity
@Table(name = "operation_logs", indexes = {
        @Index(name = "idx_olog_session", columnList = "session_id"),
        @Index(name = "idx_olog_type", columnList = "intent_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    /** 意图类型: CREATE_CLUSTER / ADD_INSTANCE / ... */
    @Column(name = "intent_type", nullable = false, length = 40)
    private String intentType;

    /** 操作名称 */
    @Column(name = "operation_name", nullable = false, length = 100)
    private String operationName;

    /** 操作参数 (JSON) */
    @Column(columnDefinition = "TEXT")
    private String parametersJson;

    /** 执行结果状态: PENDING / SUCCESS / FAILED */
    @Column(name = "op_status", nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING";

    /** 执行结果详情 */
    @Column(columnDefinition = "TEXT")
    private String resultDetail;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
