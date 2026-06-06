package com.example.installassistant.operation;

import com.example.installassistant.intent.IntentType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 创建微服务分区处理器（Stub）
 */
@Slf4j
@Component
public class CreatePartitionHandler implements OperationHandler {

    @Override
    public boolean canHandle(IntentType intentType) {
        return intentType == IntentType.CREATE_PARTITION;
    }

    @Override
    public OperationResult execute(OperationRequest request) {
        log.info("[CreatePartitionHandler] Executing, params={}", request.getParameters());
        String partitionName = request.getParameters().getOrDefault("partitionName", "默认分区");
        String msg = String.format("""
                ✅ 微服务分区创建成功
                
                分区名称: %s
                分区ID:   stub-part-001
                状态:     已就绪
                创建时间: %s
                """, partitionName, java.time.LocalDateTime.now().toString().replace("T", " "));
        return OperationResult.builder()
                .status("SUCCESS")
                .operationName("createPartition")
                .message(msg)
                .details("{\"partitionId\":\"stub-part-001\",\"partitionName\":\"" + partitionName + "\"}")
                .build();
    }
}
