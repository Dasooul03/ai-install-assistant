# 智能安装助手 — 实施计划文档

## 项目信息

| 项 | 内容 |
|---|---|
| 项目名 | 智能体平台-智能安装助手 (ai-install-assistant) |
| 构建工具 | Gradle (Groovy DSL) |
| JDK | 17 |
| Spring Boot | 3.5.x |
| 聊天模型 | OpenAI 兼容协议 (LangChain4j) |
| 嵌入模型 | 阿里 DashScope text-embedding-v3 |
| 向量数据库 | Milvus 2.x |
| 关系数据库 | MySQL 8.0 |
| Agent 框架 | LangChain4j 1.15.x |
| AI 抽象层 | Spring AI 1.1.x |

## 本机环境检查 (2025-07-17)

| 软件 | 版本 | 状态 |
|---|---|---|
| JDK | 17.0.2 (OpenJDK) | ✅ |
| Docker | 29.5.2 | ✅ |
| Docker Compose | v5.1.4 | ✅ |
| Git | 2.52.0 | ✅ |
| Gradle | — (使用 gradlew wrapper) | ✅ |

## 包结构

```
src/main/java/com/example/installassistant/
├── agent/
│   ├── AgentRouter.java
│   ├── KnowledgeQAAgent.java
│   ├── InstallationGuideAgent.java
│   ├── OperationAgent.java
│   └── DiagnosticAgent.java
├── config/
│   ├── AiConfig.java
│   ├── MilvusConfig.java
│   └── McpConfig.java
├── controller/
│   ├── ChatController.java
│   └── KnowledgeController.java
├── intent/
│   ├── IntentType.java
│   ├── IntentResult.java
│   └── IntentClassifier.java
├── model/
│   ├── Session.java
│   ├── ConversationMessage.java
│   ├── OperationLog.java
│   └── KnowledgeDocument.java
├── operation/
│   ├── OperationHandler.java
│   ├── OperationDispatcher.java
│   ├── CreateClusterHandler.java
│   ├── CreatePartitionHandler.java
│   ├── AddInstanceHandler.java
│   └── ServiceLifecycleHandler.java
├── rag/
│   ├── DocumentLoader.java
│   ├── EmbeddingService.java
│   ├── RetrievalService.java
│   ├── PromptBuilder.java
│   └── KnowledgeService.java
├── repository/
│   ├── SessionRepository.java
│   ├── ConversationMessageRepository.java
│   ├── OperationLogRepository.java
│   └── KnowledgeDocumentRepository.java
├── service/
│   ├── ChatService.java
│   ├── SessionService.java
│   └── ConversationHistoryService.java
└── AiInstallAssistantApplication.java
```

## Phase 1 — 项目骨架与基础设施

- Gradle Spring Boot 项目初始化
- docker-compose.yml (Milvus + MySQL)
- application.yml 全部配置
- 完整包结构创建
- Logback 日志配置
- 验证：编译通过 + bootRun 无报错

## Phase 2 — 数据模型与 MySQL 持久层

- JPA Entity ×4
- Repository ×4
- SessionService + ConversationHistoryService
- 验证：ddl-auto 建表

## Phase 3 — RAG 系统

- EmbeddingModel Bean (DashScope)
- MilvusVectorStore Bean
- DocumentLoader (md/txt 分段)
- EmbeddingService (批量嵌入)
- RetrievalService (相似检索)
- PromptBuilder (模板填充)
- KnowledgeService + KnowledgeController

## Phase 4 — 意图识别模块

- IntentType 枚举 (8种)
- IntentResult record
- IntentClassifier (few-shot LLM)
- prompts/intent-classifier.st

## Phase 5 — Agent 路由器与多智能体

- AgentRouter @AiService
- KnowledgeQAAgent
- InstallationGuideAgent
- OperationAgent
- DiagnosticAgent
- prompts/router-system.st

## Phase 6 — 操作识别与任务自动化调度

- OperationHandler 接口
- 4 个 Handler 实现 (Stub)
- OperationDispatcher

## Phase 7 — MCP 集成

- MCP Server (WebMVC SSE)
- McpToolRegistry

## Phase 8 — Chat 入口与全链路串联

- ChatController (SSE)
- ChatService 全链路编排
- SSE 流式输出

## Phase 9 — 最终测试、文档与容器化

- Dockerfile 多阶段构建
- README.md
- docker-compose 完善
- E2E 测试
