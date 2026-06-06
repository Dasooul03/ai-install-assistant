#!/usr/bin/env pwsh
# 一键导入示例知识库到智能安装助手 (PowerShell)
# 用法: .\scripts\init-knowledge.ps1 [-BaseUrl http://localhost:8080]

param([string]$BaseUrl = "http://localhost:8080")

Write-Host "=== 初始化知识库 ===" -ForegroundColor Cyan
Write-Host "目标服务: $BaseUrl"
Write-Host ""

# 1. 安装手册
Write-Host "[1/3] 上传安装手册..." -ForegroundColor Yellow
$manual = @{
    content = @"
## 安装准备

在安装之前，请确保以下软件已安装：
- JDK 17+
- Docker 20.10+
- Docker Compose v2

## 安装步骤

1. 克隆项目代码
2. 复制 .env.example 为 .env 并填写配置
3. 运行 docker-compose up -d 启动 Milvus 和 MySQL
4. 运行 ./gradlew bootRun 启动应用
"@
    fileName = "安装手册.md"
    docType = "MANUAL"
} | ConvertTo-Json

try {
    $resp = Invoke-RestMethod -Uri "$BaseUrl/api/knowledge/upload/text" -Method Post -Body $manual -ContentType "application/json"
    Write-Host "  完成: $($resp.document.id)" -ForegroundColor Green
} catch {
    Write-Host "  失败: $_" -ForegroundColor Red
}

# 2. FAQ
Write-Host "[2/3] 上传 FAQ..." -ForegroundColor Yellow
$faq = @{
    content = @"
## 常见问题

### Q: 启动失败怎么办？
检查端口是否被占用：19530(Milvus)、3306(MySQL)、8080(应用)。

### Q: 如何重置 Milvus？
运行 docker-compose down -v 清除数据卷，然后重新启动。

### Q: embedding 模型如何切换？
修改 application.yml 中的 spring.ai.openai.embedding.options.model 为其他模型名。
"@
    fileName = "FAQ.md"
    docType = "FAQ"
} | ConvertTo-Json

try {
    $resp = Invoke-RestMethod -Uri "$BaseUrl/api/knowledge/upload/text" -Method Post -Body $faq -ContentType "application/json"
    Write-Host "  完成: $($resp.document.id)" -ForegroundColor Green
} catch {
    Write-Host "  失败: $_" -ForegroundColor Red
}

# 3. 配置文档
Write-Host "[3/3] 上传配置文档..." -ForegroundColor Yellow
$config = @{
    content = @"
## 配置说明

### LLM 配置
langchain4j.open-ai.chat-model.api-key: 填写 LLM API Key
langchain4j.open-ai.chat-model.model-name: 模型名称，默认 gpt-4o-mini

### Milvus 配置
spring.ai.vectorstore.milvus.client.host: Milvus 服务地址
spring.ai.vectorstore.milvus.embedding-dimension: 向量维度，text-embedding-v3 为 1024

### 操作后端
操作类意图会下发到 OperationDispatcher，当前为 Stub 模式。
对接真实后端时替换 OperationHandler 实现。
"@
    fileName = "配置文档.md"
    docType = "CONFIG"
} | ConvertTo-Json

try {
    $resp = Invoke-RestMethod -Uri "$BaseUrl/api/knowledge/upload/text" -Method Post -Body $config -ContentType "application/json"
    Write-Host "  完成: $($resp.document.id)" -ForegroundColor Green
} catch {
    Write-Host "  失败: $_" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== 知识库初始化完成 ===" -ForegroundColor Cyan
Write-Host "验证: Invoke-RestMethod $BaseUrl/api/knowledge/list | ConvertTo-Json"
