## 安装准备

在安装智能安装助手之前，请确保以下软件已安装：
- JDK 17 或更高版本
- Docker 20.10+ 和 Docker Compose v2
- 至少 8GB 可用内存
- 至少 20GB 可用磁盘空间

## 安装步骤

1. 克隆项目源码: `git clone <repo-url>`
2. 进入项目目录: `cd ai-install-assistant`
3. 复制环境变量模板: `cp .env.example .env`
4. 编辑 `.env` 填写 API Key（LLM_API_KEY 必填）
5. 启动基础设施: `docker compose up -d`（启动 Milvus + MySQL）
6. 启动应用: `./gradlew bootRun --args='--spring.profiles.active=h2'`
7. 浏览器打开: http://localhost:8080

## 配置说明

### LLM 聊天模型
使用 DeepSeek (OpenAI 兼容协议)，在 `.env` 中设置:
- LLM_API_KEY=sk-xxx
- LLM_BASE_URL=https://api.deepseek.com/v1
- LLM_MODEL_NAME=deepseek-chat

### 数据库
- MySQL 8.0（生产环境）: 端口 3306，数据库名 install_assistant
- H2（开发环境）: 文件模式，自动创建

## 操作命令说明

### 创建集群
输入示例: "帮我创建一个3节点的集群" 或 "新建一个叫prod的集群"
参数: clusterName（可选）、nodeCount（可选，默认3）
Agent 会自动分类为 CREATE_CLUSTER 意图并下发执行

### 增加实例
输入示例: "给我的user-service增加5个实例" 
参数: serviceName、count
Agent 会提取微服务名称和数量并执行

### 微服务启停
输入示例: "重启order-service" 或 "停止payment-service"
参数: serviceName、action（start/stop/restart）

## 常见问题

### Q: 启动时提示端口被占用？
检查以下端口是否空闲: 8080(应用)、19530(Milvus)、3306(MySQL)。使用 `netstat -ano | findstr <端口>` 查看占用情况。

### Q: LLM 调用返回错误？
检查 `.env` 中的 LLM_API_KEY 是否正确，以及网络能否访问 `api.deepseek.com`。

### Q: 如何查看日志？
日志文件位于项目 `logs/` 目录下，使用 `tail -f logs/install-assistant.log` 实时查看。

## Docker 安装指南

### Windows 安装 Docker
1. 下载 Docker Desktop for Windows: https://www.docker.com/products/docker-desktop/
2. 双击安装包，按照向导完成安装
3. 安装完成后重启电脑
4. 启动 Docker Desktop，等待右下角鲸鱼图标变绿
5. 打开 PowerShell 验证: `docker --version`

注意: Windows 需要启用 WSL 2（Windows Subsystem for Linux），Docker Desktop 安装程序会自动引导配置。

### macOS 安装 Docker
1. 下载 Docker Desktop for Mac (根据芯片选择 Intel 或 Apple Silicon): https://www.docker.com/products/docker-desktop/
2. 将 Docker.app 拖入 Applications 文件夹
3. 启动 Docker Desktop，等待菜单栏图标变绿
4. 打开终端验证: `docker --version`

或使用 Homebrew: `brew install --cask docker`

### Linux (Ubuntu/Debian) 安装 Docker
```bash
# 1. 更新包管理器
sudo apt update
# 2. 安装依赖
sudo apt install -y ca-certificates curl
# 3. 添加 Docker 官方 GPG 密钥
sudo install -m 0755 -d /etc/apt/keyrings
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
# 4. 添加 Docker 仓库
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
# 5. 更新并安装
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
# 6. 验证
docker --version
docker compose version
```

### 安装后验证
```bash
# 运行测试容器
docker run hello-world
# 查看版本
docker --version
docker compose version
```

### Docker 常用命令
- `docker ps` — 查看运行中的容器
- `docker ps -a` — 查看所有容器
- `docker images` — 查看本地镜像
- `docker compose up -d` — 启动 docker-compose 服务（后台）
- `docker compose down` — 停止并删除服务
- `docker compose logs -f` — 查看实时日志
- `docker system prune -a` — 清理未使用的镜像和容器
