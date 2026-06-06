package com.example.installassistant.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 会话实体
 */
@Entity
@Table(name = "chat_sessions", indexes = {
        @Index(name = "idx_session_status", columnList = "session_status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 会话状态: ACTIVE / CLOSED */
    @Column(name = "session_status", nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    /** 会话标题（由首条消息摘要生成） */
    @Column(length = 200)
    private String title;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
