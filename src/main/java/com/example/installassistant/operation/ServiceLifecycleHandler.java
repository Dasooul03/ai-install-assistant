package com.example.installassistant.operation;

import com.example.installassistant.intent.IntentType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 微服务启停处理器（Stub）
 */
@Slf4j
@Component
public class ServiceLifecycleHandler implements OperationHandler {

    @Override
    public boolean canHandle(IntentType intentType) {
        return intentType == IntentType.SERVICE_LIFECYCLE;
    }

    @Override
    public OperationResult execute(OperationRequest request) {
        log.info("[ServiceLifecycleHandler] Executing, params={}", request.getParameters());
        String action = request.getParameters().getOrDefault("action", "start");
        String service = request.getParameters().getOrDefault("serviceName",
                request.getParameters().getOrDefault("service", "unknown"));
        String actionDesc = switch (action) {
            case "start" -> "启动";
            case "stop" -> "停止";
            case "restart" -> "重启";
            default -> action;
        };
        String statusEmoji = action.equals("stop") ? "⏹️" : "✅";
        String msg = String.format("""
                %s 微服务%s成功
                
                微服务名称: %s
                执行操作:   %s
                当前状态:   %s
                响应时间:   %dms
                操作时间:   %s
                """, statusEmoji, actionDesc, service, actionDesc,
                action.equals("stop") ? "已停止" : "运行中",
                (int)(Math.random() * 200 + 50),
                java.time.LocalDateTime.now().toString().replace("T", " "));
        return OperationResult.builder()
                .status("SUCCESS")
                .operationName("serviceLifecycle")
                .message(msg)
                .details("{\"service\":\"" + service + "\",\"action\":\"" + action + "\",\"status\":\"running\"}")
                .build();
    }
}
