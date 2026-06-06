package com.example.installassistant.operation;

import com.example.installassistant.intent.IntentType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 增加实例处理器（Stub）
 */
@Slf4j
@Component
public class AddInstanceHandler implements OperationHandler {

    @Override
    public boolean canHandle(IntentType intentType) {
        return intentType == IntentType.ADD_INSTANCE;
    }

    @Override
    public OperationResult execute(OperationRequest request) {
        log.info("[AddInstanceHandler] Executing, params={}", request.getParameters());
        String serviceName = request.getParameters().getOrDefault("serviceName", "unknown-service");
        String count = request.getParameters().getOrDefault("count", "1");
        int cnt = Integer.parseInt(count);
        String msg = String.format("""
                ✅ 实例增加成功
                
                微服务名称: %s
                新增实例数: %d 个
                实例ID:     %s
                目标分区:   default
                当前状态:   运行中
                操作时间:   %s
                """, serviceName, cnt,
                generateInstanceIds(serviceName, cnt),
                java.time.LocalDateTime.now().toString().replace("T", " "));
        return OperationResult.builder()
                .status("SUCCESS")
                .operationName("addInstance")
                .message(msg)
                .details("{\"serviceName\":\"" + serviceName + "\",\"instanceCount\":" + cnt + ",\"partition\":\"default\"}")
                .build();
    }

    private String generateInstanceIds(String serviceName, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= count; i++) {
            if (i > 1) sb.append(", ");
            sb.append(serviceName).append("-inst-").append(String.format("%03d", i));
        }
        return sb.toString();
    }
}
