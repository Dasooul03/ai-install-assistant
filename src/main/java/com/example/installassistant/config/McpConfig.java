package com.example.installassistant.config;

import com.example.installassistant.intent.IntentType;
import com.example.installassistant.operation.OperationDispatcher;
import com.example.installassistant.operation.OperationRequest;
import com.example.installassistant.operation.OperationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * MCP Server 配置 — 通过 Spring AI @Tool 注解暴露操作工具给 MCP 客户端
 *
 * MCP Server 由 spring-ai-starter-mcp-server-webmvc 自动配置，
 * 路径: /mcp/sse (SSE transport)
 */
@Slf4j
@Configuration
public class McpConfig {

    private final OperationDispatcher operationDispatcher;

    public McpConfig(OperationDispatcher operationDispatcher) {
        this.operationDispatcher = operationDispatcher;
        log.info("[McpConfig] MCP Server tools registered: createCluster, createPartition, addInstance, manageService");
    }

    /**
     * 将 MCP 工具方法注册为 Spring Bean
     * Spring AI MCP Server 会自动扫描 @Tool 注解
     */
    @Bean
    public McpTools mcpTools() {
        return new McpTools();
    }

    /**
     * MCP 工具集合 — 暴露给 MCP 客户端的操作
     */
    public class McpTools {

        @Tool(description = "创建新集群")
        public String createCluster(
                @ToolParam(description = "集群名称") String clusterName,
                @ToolParam(description = "节点数量", required = false) String nodeCount) {
            log.info("[MCP] createCluster called: clusterName={}, nodeCount={}", clusterName, nodeCount);
            OperationRequest req = new OperationRequest(
                    null, IntentType.CREATE_CLUSTER.name(),
                    Map.of("clusterName", clusterName, "nodeCount", nodeCount != null ? nodeCount : "3"),
                    "MCP: createCluster"
            );
            OperationResult result = operationDispatcher.dispatch(req);
            return result.getMessage();
        }

        @Tool(description = "创建微服务分区")
        public String createPartition(
                @ToolParam(description = "分区名称") String partitionName) {
            log.info("[MCP] createPartition called: partitionName={}", partitionName);
            OperationRequest req = new OperationRequest(
                    null, IntentType.CREATE_PARTITION.name(),
                    Map.of("partitionName", partitionName),
                    "MCP: createPartition"
            );
            OperationResult result = operationDispatcher.dispatch(req);
            return result.getMessage();
        }

        @Tool(description = "给微服务增加实例")
        public String addInstance(
                @ToolParam(description = "微服务名称") String serviceName,
                @ToolParam(description = "实例数量", required = false) String count) {
            log.info("[MCP] addInstance called: serviceName={}, count={}", serviceName, count);
            OperationRequest req = new OperationRequest(
                    null, IntentType.ADD_INSTANCE.name(),
                    Map.of("serviceName", serviceName, "count", count != null ? count : "1"),
                    "MCP: addInstance"
            );
            OperationResult result = operationDispatcher.dispatch(req);
            return result.getMessage();
        }

        @Tool(description = "管理微服务生命周期: start/stop/restart")
        public String manageService(
                @ToolParam(description = "微服务名称") String serviceName,
                @ToolParam(description = "操作: start, stop, restart") String action) {
            log.info("[MCP] manageService called: serviceName={}, action={}", serviceName, action);
            OperationRequest req = new OperationRequest(
                    null, IntentType.SERVICE_LIFECYCLE.name(),
                    Map.of("serviceName", serviceName, "action", action),
                    "MCP: manageService"
            );
            OperationResult result = operationDispatcher.dispatch(req);
            return result.getMessage();
        }
    }
}
