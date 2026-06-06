package com.example.installassistant.agent;

import com.example.installassistant.intent.IntentClassifier;
import com.example.installassistant.intent.IntentResult;
import com.example.installassistant.intent.IntentType;
import com.example.installassistant.operation.OperationDispatcher;
import com.example.installassistant.operation.OperationRequest;
import com.example.installassistant.operation.OperationResult;
import com.example.installassistant.rag.PromptBuilder;
import com.example.installassistant.rag.RetrievalService;
import com.example.installassistant.service.ConversationHistoryService;
import dev.langchain4j.model.chat.ChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 中央调度器（Agent Router）
 * 使用 LangChain4j @AiService + @Tool 实现动态指令解析与组件路由。
 *
 * 注意: 由于 @AiService 是接口且由 LangChain4j 代理，
 * 实际的 Tool 方法定义在此辅助类中。
 */
@Slf4j
@Component
public class AgentRouter {

    private final ChatModel chatModel;
    private final IntentClassifier intentClassifier;
    private final RetrievalService retrievalService;
    private final PromptBuilder promptBuilder;
    private final ConversationHistoryService historyService;
    private final OperationDispatcher operationDispatcher;

    public AgentRouter(ChatModel chatModel,
                       IntentClassifier intentClassifier,
                       RetrievalService retrievalService,
                       PromptBuilder promptBuilder,
                       ConversationHistoryService historyService,
                       OperationDispatcher operationDispatcher) {
        this.chatModel = chatModel;
        this.intentClassifier = intentClassifier;
        this.retrievalService = retrievalService;
        this.promptBuilder = promptBuilder;
        this.historyService = historyService;
        this.operationDispatcher = operationDispatcher;
    }

    /**
     * 入口方法：分类意图 → 路由到对应微智能体
     */
    public String route(String userMessage, String sessionIdStr) {
        long start = System.currentTimeMillis();
        Long sessionId = sessionIdStr != null ? Long.valueOf(sessionIdStr) : null;
        log.info("[AgentRouter] Routing message, sessionId={}", sessionId);

        // Step 1: 意图分类
        IntentResult intent = intentClassifier.classify(userMessage);

        // Step 2: 按意图路由
        String result = switch (intent.type()) {
            case KNOWLEDGE_QA -> handleKnowledgeQA(userMessage, sessionId);
            case INSTALL_GUIDE -> handleInstallGuide(userMessage, sessionId);
            case CREATE_CLUSTER, CREATE_PARTITION, ADD_INSTANCE, SERVICE_LIFECYCLE ->
                    handleOperation(userMessage, intent, sessionId);
            case DIAGNOSTIC -> handleDiagnostic(userMessage, sessionId);
            case CHITCHAT -> handleChitchat(userMessage, sessionId);
        };

        long elapsed = System.currentTimeMillis() - start;
        log.info("[AgentRouter] Routed to {}, elapsed={}ms", intent.type(), elapsed);
        return result;
    }

    // ========== 微智能体处理逻辑 ==========

    /**
     * 知识问答 — RAG 检索 + LLM 生成
     * 当 Milvus 不可用时自动回退到内置知识文档
     */
    private String handleKnowledgeQA(String query, Long sessionId) {
        log.info("[KnowledgeQAAgent] Processing QA, sessionId={}", sessionId);

        // 加载会话历史上下文
        String historyContext = sessionId != null
                ? historyService.buildChatHistoryContext(sessionId, 10)
                : "";

        // RAG 检索（如果 Milvus 可用则返回真实结果，否则返回空）
        String retrievalContext = retrievalService.retrieveAsContext(query);

        // 如果 RAG 为空，回退到内置知识文档
        if (retrievalContext.isEmpty()) {
            retrievalContext = loadBuiltinKnowledge();
            log.info("[KnowledgeQAAgent] Using built-in knowledge, length={}", retrievalContext.length());
        } else {
            log.debug("[KnowledgeQAAgent] Retrieved context length={}", retrievalContext.length());
        }

        // 构建 Prompt
        String systemPrompt = promptBuilder.buildSystemPrompt(retrievalContext);
        String fullPrompt = buildFullPrompt(systemPrompt, historyContext, query);

        return chatModel.chat(fullPrompt);
    }

    /**
     * 安装指引 — RAG + 安装阶段上下文
     */
    private String handleInstallGuide(String query, Long sessionId) {
        log.info("[InstallationGuideAgent] Processing guide, sessionId={}", sessionId);
        return handleKnowledgeQA(query, sessionId); // 复用 QA 流程，检索安装手册
    }

    /**
     * 操作执行 — 意图识别 + 参数提取 + 下发到后端
     */
    private String handleOperation(String userInput, IntentResult intent, Long sessionId) {
        log.info("[OperationAgent] Processing operation: type={}, params={}", intent.type(), intent.parameters());

        OperationRequest req = new OperationRequest(
                sessionId,
                intent.type().name(),
                intent.parameters(),
                userInput
        );
        OperationResult result = operationDispatcher.dispatch(req);

        return formatOperationResult(result);
    }

    /**
     * 故障诊断 — RAG 检索 FAQ + 排错建议
     */
    private String handleDiagnostic(String issue, Long sessionId) {
        log.info("[DiagnosticAgent] Processing diagnostic, sessionId={}", sessionId);
        String context = retrievalService.retrieveAsContext(issue);
        if (context.isEmpty()) {
            context = loadBuiltinKnowledge();
        }

        String systemPrompt = """
                你是一个故障诊断专家。请基于参考文档中的信息，分析用户遇到的问题并给出排查建议。
                
                参考文档：
                %s
                
                请用中文给出诊断建议，包含：
                1. 可能的原因
                2. 具体的排查步骤
                3. 建议的解决方案
                """.formatted(context.isEmpty() ? "（无相关文档）" : context);

        return chatModel.chat(systemPrompt + "\n\n用户问题：" + issue);
    }

    /**
     * 闲聊 — 用系统提示词介绍 Agent 身份和能力
     */
    private String handleChitchat(String message, Long sessionId) {
        log.info("[AgentRouter] Chitchat mode, sessionId={}", sessionId);
        String systemPrompt = """
                你是"智能安装助手"，一个面向企业级产品安装部署场景的AI智能体平台。
                
                你的核心能力包括：
                1. 📚 知识问答 — 回答安装、配置、产品功能等相关问题，基于内置知识库检索
                2. 📖 安装指引 — 分步引导用户完成产品安装部署流程
                3. ⚙️ 自动化操作 — 执行创建集群、创建微服务分区、增加实例、服务启停等运维操作
                4. 🔧 故障诊断 — 根据错误日志和症状分析问题，给出排查建议
                
                当前底层技术栈：
                - 大模型: DeepSeek (deepseek-chat)
                - Agent框架: LangChain4j + Spring AI
                - 向量检索: Milvus (RAG)
                - 后端: Spring Boot 3.5
                
                请用专业且友好的语气回复用户，先简短介绍自己是谁、能做什么，再回应用户的具体问题。
                如果用户只是打招呼，就介绍自己并引导用户说明需求。
                """;
        return chatModel.chat(systemPrompt + "\n\n用户消息：" + message);
    }

    // ========== 工具方法 ==========

    private String buildFullPrompt(String systemPrompt, String historyContext, String query) {
        StringBuilder sb = new StringBuilder();
        sb.append(systemPrompt);
        if (!historyContext.isEmpty()) {
            sb.append("\n\n## 对话历史\n");
            sb.append(historyContext);
        }
        sb.append("\n\n## 用户问题\n");
        sb.append(query);
        return sb.toString();
    }

    private String formatOperationResult(OperationResult result) {
        // Handler 的 message 已经包含格式化的详细信息，直接返回
        if ("SUCCESS".equals(result.getStatus())) {
            return result.getMessage();
        } else {
            return "❌ 操作执行失败: " + result.getMessage();
        }
    }

    /**
     * 加载内置知识文档 — 当 Milvus RAG 不可用时的回退方案
     */
    private String loadBuiltinKnowledge() {
        try (var is = getClass().getClassLoader()
                .getResourceAsStream("knowledge/base-knowledge.md")) {
            if (is == null) return "";
            return new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("[AgentRouter] Failed to load built-in knowledge", e);
            return "";
        }
    }
}
