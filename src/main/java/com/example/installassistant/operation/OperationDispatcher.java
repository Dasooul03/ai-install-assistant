package com.example.installassistant.operation;

import com.example.installassistant.model.OperationLog;
import com.example.installassistant.repository.OperationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 操作调度器 — 根据意图匹配 Handler 并执行
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationDispatcher {

    private final List<OperationHandler> handlers;
    private final OperationLogRepository operationLogRepository;

    /**
     * 分发并执行操作
     */
    public OperationResult dispatch(OperationRequest request) {
        log.info("[OperationDispatcher] Dispatching operation: intent={}, params={}",
                request.getIntentType(), request.getParameters());

        OperationHandler handler = handlers.stream()
                .filter(h -> {
                    try {
                        return h.canHandle(com.example.installassistant.intent.IntentType
                                .valueOf(request.getIntentType()));
                    } catch (IllegalArgumentException e) {
                        return false;
                    }
                })
                .findFirst()
                .orElse(null);

        if (handler == null) {
            log.warn("[OperationDispatcher] No handler found for intent: {}", request.getIntentType());
            return OperationResult.builder()
                    .status("FAILED")
                    .operationName("unknown")
                    .message("不支持的操作类型: " + request.getIntentType())
                    .build();
        }

        OperationResult result;
        try {
            result = handler.execute(request);
            log.info("[OperationDispatcher] Operation completed: status={}", result.getStatus());
        } catch (Exception e) {
            log.error("[OperationDispatcher] Operation failed", e);
            result = OperationResult.builder()
                    .status("FAILED")
                    .operationName(handler.getClass().getSimpleName())
                    .message("操作执行异常: " + e.getMessage())
                    .build();
        }

        // 持久化操作日志
        persistLog(request, result);

        return result;
    }

    private void persistLog(OperationRequest request, OperationResult result) {
        try {
            OperationLog logEntry = OperationLog.builder()
                    .sessionId(request.getSessionId())
                    .intentType(request.getIntentType())
                    .operationName(result.getOperationName())
                    .parametersJson(request.getParameters() != null
                            ? request.getParameters().toString() : "{}")
                    .status(result.getStatus())
                    .resultDetail(result.getDetails())
                    .build();
            operationLogRepository.save(logEntry);
            log.debug("[OperationDispatcher] Logged operation to DB: id={}", logEntry.getId());
        } catch (Exception e) {
            log.error("[OperationDispatcher] Failed to persist operation log", e);
        }
    }
}
