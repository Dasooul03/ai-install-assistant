package com.example.installassistant.operation;

import com.example.installassistant.intent.IntentType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 创建集群处理器（Stub）
 */
@Slf4j
@Component
public class CreateClusterHandler implements OperationHandler {

    @Override
    public boolean canHandle(IntentType intentType) {
        return intentType == IntentType.CREATE_CLUSTER;
    }

    @Override
    public OperationResult execute(OperationRequest request) {
        log.info("[CreateClusterHandler] Executing, params={}", request.getParameters());
        String clusterName = request.getParameters().getOrDefault("clusterName", "默认集群");
        String nodeCount = request.getParameters().getOrDefault("nodeCount", "3");
        String msg = String.format("""
                ✅ 集群创建成功 [%s]
                
                集群名称: %s
                节点数量: %s 个
                集群ID:   stub-cluster-001
                状态:     运行中
                创建时间: %s
                """, clusterName, clusterName, nodeCount, java.time.LocalDateTime.now().toString().replace("T", " "));
        return OperationResult.builder()
                .status("SUCCESS")
                .operationName("createCluster")
                .message(msg)
                .details("{\"clusterId\":\"stub-cluster-001\",\"clusterName\":\"" + clusterName + "\",\"nodes\":" + nodeCount + "}")
                .build();
    }
}
