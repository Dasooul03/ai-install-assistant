package com.example.installassistant.operation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 操作请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperationRequest {
    /** 会话 ID */
    private Long sessionId;
    /** 意图类型 */
    private String intentType;
    /** 操作参数 */
    private Map<String, String> parameters;
    /** 原始用户输入 */
    private String originalInput;
}
