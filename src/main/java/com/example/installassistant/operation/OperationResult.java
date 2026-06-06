package com.example.installassistant.operation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 操作执行结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperationResult {
    /** SUCCESS / FAILED */
    private String status;
    /** 操作名称 */
    private String operationName;
    /** 结果消息 */
    private String message;
    /** 详细信息 (JSON) */
    private String details;
}
