#!/bin/bash
# 一键导入示例知识库到智能安装助手
# 用法: ./scripts/init-knowledge.sh [BASE_URL]

BASE_URL=${1:-http://localhost:8080}

echo "=== 初始化知识库 ==="
echo "目标服务: $BASE_URL"
echo ""

# 1. 安装手册
echo "[1/3] 上传安装手册..."
curl -s -X POST "$BASE_URL/api/knowledge/upload/text" \
  -H "Content-Type: application/json" \
  -d '{
    "content": "## 安装准备\n\n在安装之前，请确保以下软件已安装：\n- JDK 17+\n- Docker 20.10+\n- Docker Compose v2\n\n## 安装步骤\n\n1. 克隆项目代码\n2. 复制 .env.example 为 .env 并填写配置\n3. 运行 docker-compose up -d 启动 Milvus 和 MySQL\n4. 运行 ./gradlew bootRun 启动应用",
    "fileName": "安装手册.md",
    "docType": "MANUAL"
  }' | jq .

# 2. FAQ
echo "[2/3] 上传 FAQ..."
curl -s -X POST "$BASE_URL/api/knowledge/upload/text" \
  -H "Content-Type: application/json" \
  -d '{
    "content": "## 常见问题\n\n### Q: 启动失败怎么办？\n检查端口是否被占用：19530(Milvus)、3306(MySQL)、8080(应用)。\n\n### Q: 如何重置 Milvus？\n运行 docker-compose down -v 清除数据卷，然后重新启动。\n\n### Q: embedding 模型如何切换？\n修改 application.yml 中的 spring.ai.dashscope.embedding.model 为其他模型名。",
    "fileName": "FAQ.md",
    "docType": "FAQ"
  }' | jq .

# 3. 配置文档
echo "[3/3] 上传配置文档..."
curl -s -X POST "$BASE_URL/api/knowledge/upload/text" \
  -H "Content-Type: application/json" \
  -d '{
    "content": "## 配置说明\n\n### LLM 配置\nlangchain4j.open-ai.chat-model.api-key: 填写 LLM API Key\nlangchain4j.open-ai.chat-model.model-name: 模型名称，默认 gpt-4o-mini\n\n### Milvus 配置\nspring.ai.vectorstore.milvus.client.host: Milvus 服务地址\nspring.ai.vectorstore.milvus.embedding-dimension: 向量维度，text-embedding-v3 为 1024\n\n### 操作后端\n操作类意图会下发到 OperationDispatcher，当前为 Stub 模式，对接真实后端时替换 OperationHandler 实现。",
    "fileName": "配置文档.md",
    "docType": "CONFIG"
  }' | jq .

echo ""
echo "=== 知识库初始化完成 ==="
echo "验证: curl $BASE_URL/api/knowledge/list | jq ."
