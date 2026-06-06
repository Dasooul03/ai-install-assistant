package com.example.installassistant.operation;

import com.example.installassistant.intent.IntentType;

/**
 * 操作处理器接口
 */
public interface OperationHandler {

    /** 是否可处理该意图类型 */
    boolean canHandle(IntentType intentType);

    /** 执行操作 */
    OperationResult execute(OperationRequest request);
}
