package com.example.installassistant.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 会话消息实体
 */
@Entity
@Table(name = "conversation_messages", indexes = {
        @Index(name = "idx_msg_session", columnList = "session_id"),
        @Index(name = "idx_msg_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    /** 角色: USER / ASSISTANT / SYSTEM */
    @Column(name = "msg_role", nullable = false, length = 20)
    private String role;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /** 消息元数据 (JSON): token 数、意图等 */
    @Column(name = "msg_metadata", columnDefinition = "TEXT")
    private String metadata;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
