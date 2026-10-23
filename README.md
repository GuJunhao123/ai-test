# AI Test · 多模型对比与评测平台

基于 **Spring Boot + Spring AI + Vue 3** 的全栈学习实践项目，通过 OpenRouter 接入多种大模型，围绕“同一问题，不同模型如何回答”实现并行对话、提示词实验、场景批量评测、评分报告与成本统计。

本仓库记录了一个**从环境搭建、数据库设计、后端接口、前端页面，到异步任务和容器部署的完整项目实现过程**。不仅包含接口或页面示例，也保留了分阶段学习笔记。可按照下文配置依赖、初始化数据并启动前后端。

> 项目未经过生产级验证的服务。

## 功能概览

| 模块 | 实现内容 |
| --- | --- |
| 多模型对比 | 同一提示词并行请求多个模型，SSE 流式显示，保存对话上下文与消息 |
| 匿名对战 | 隐藏模型身份进行回答对比与评价 |
| Prompt Lab | 同一模型下测试多个提示词变体，区分各变体上下文 |
| 提示词管理 | 模板维护、提示词优化、输入护栏与代码块预览 |
| 批量评测 | 创建场景与测试任务，RabbitMQ 异步消费，WebSocket 推送进度 |
| AI 评分 | 多评委并行评分、评分一致性分析及报告汇总 |
| 结果报告 | 雷达图、柱状图、任务对比及 PDF 导出 |
| 用量与预算 | Token、调用量、模型成本统计，日/月预算与阈值设置 |
| 多模态 | 图片上传、图片生成、支持模型的联网搜索；具体能力取决于服务商与模型 |
| 基础能力 | 用户登录、Redis Session、权限校验、限流、缓存、接口文档与健康检查 |

## 技术栈

- **后端**：Java 21、Spring Boot 3.5.9、Spring AI 1.1.2、Spring MVC / WebFlux、Reactor、MyBatis-Flex 1.11.1。
- **前端**：Vue 3、TypeScript、Vite 7、Ant Design Vue、Pinia、Vue Router、Axios。
- **数据与消息**：MySQL 8、Redis、Redisson、RabbitMQ、WebSocket / STOMP。
- **展示与工具**：ECharts、Monaco Editor、Markdown 渲染、html2canvas、jsPDF、Knife4j。
- **外部服务与部署**：OpenRouter、腾讯云 COS、Docker Compose、Nginx。

## 从零实现的学习路线

学习笔记以实际开发中的问题为主线，建议按顺序结合代码阅读。

| 阶段 | 笔记 | 从学习到实现 |
| --- | --- | --- |
| 1. 搭好基础工程 | [n1：环境与基础能力](notes/n1.md) | MySQL、Redis、接口文档、代码生成、登录态、全局异常、AOP 权限与日志，以及 Spring AI / OpenRouter 接入 |
| 2. 打通流式对话 | [n2：SSE 与多模型对话](notes/n2.md) | 对话与消息建模、历史上下文、Flux 流式返回、多个模型流合并、前后端连接关闭与消息隔离 |
| 3. 从实时交互到后台任务 | [n3：Prompt Lab 与异步评测](notes/n3.md) | 提示词变体隔离、代码预览、事务提交后发消息、RabbitMQ Worker、WebSocket 进度通知 |
| 4. 让评测结果可解释 | [n4：评分、报告与工程能力](notes/n4.md) | 多评委评分、结构化 JSON、图表与 PDF、缓存、优先级队列、限流及 SSE 异常处理 |
| 5. 处理并发与成本问题 | [n5：稳定性、统计与预算](notes/n5.md) | 输入护栏、重试适用边界、任务进度一致性、原子更新、成本统计与预算、前端分包 |
| 6. 交付与能力扩展 | [n6：部署与多模态](notes/n6.md) | Docker 多阶段构建、Nginx、Compose、COS 图片存储、图片生成与联网搜索 |

几个关键实践：

- **SSE 与 WebSocket 分工**：聊天响应使用 SSE；后台评测进度使用 WebSocket / STOMP。
- **事务与消息一致性**：任务事务提交后再发消息，避免消费者先收到消息却查不到任务。
- **上下文隔离**：Prompt Lab 根据消息轮次和变体索引管理历史，避免不同提示词实验串流。
- **并行不等于随意重试**：模型请求、评分与任务进度分别处理；阻塞式重试不能直接套到流式响应或 MQ Worker。
- **统计与查询分层**：结合 Redis、用户模型聚合表和原始数据，兼顾查询效率与统计重建。

## 仓库结构

```text
ai-model-test/
├── README.md
├── notes/                         # n1 ~ n6 学习笔记
└── ai-test-s/
    ├── pom.xml                    # Java 后端依赖与构建
    ├── sql/create_table.sql       # 建库、建表与演示数据
    ├── src/main/java/com/yupi/template/
    │   ├── controller/            # HTTP 接口
    │   ├── service/               # 对话、评测、评分、报告、统计
    │   ├── worker/                # RabbitMQ 任务消费
    │   ├── job/                   # 模型定时同步
    │   ├── config/                # AI、Redis、MQ、COS、WebSocket 配置
    │   └── model/                 # 实体、请求对象与返回对象
    ├── src/main/resources/        # Spring 配置与 Mapper
    ├── src/test/                  # 集成测试与开发辅助代码
    ├── frontend/                  # Vue 前端
    ├── Dockerfile
    └── docker-compose.yml
```

## 从零到本地可运行

以下以 **Windows PowerShell + Docker Desktop + 本地 Java / Node.js** 为例。其他系统可采用相同流程，但需调整命令语法。

### 1. 准备环境与服务账号

安装 Git、JDK 21、Maven 3.9+、Node.js 22.12+ 和 Docker Desktop（含 Compose）。确保终端可以执行：

```powershell
git --version
java -version
mvn -version
node --version
npm --version
docker compose version
```

另外准备：

- 可用的 OpenRouter API Key、模型访问权限及必要余额。
- 腾讯云 COS 的访问密钥、存储桶、地域与访问域名。当前 COS Client **无条件初始化**，不要假设不填密钥也能启动；完整图片链路需要真实可用的桶及相应权限。
- 与 RabbitMQ 3.12 兼容的 `rabbitmq_delayed_message_exchange` 插件。当前配置声明了 `x-delayed-message` 交换机，仓库默认镜像并未内置该插件。

### 2. 克隆代码并设置环境变量

```powershell
git clone git@github.com:GuJunhao123/ai-test.git
Set-Location ai-test/ai-test-s

$env:MYSQL_USERNAME = 'root'
$env:MYSQL_PASSWORD = '<自行设置数据库强密码>'
$env:RABBITMQ_USERNAME = 'ai_eval'
$env:RABBITMQ_PASSWORD = '<自行设置消息队列强密码>'
$env:OPENROUTER_API_KEY = '<你的 OpenRouter API Key>'
$env:TENCENT_COS_ACCESS_KEY = '<你的 COS AccessKey>'
$env:TENCENT_COS_SECRET_KEY = '<你的 COS SecretKey>'
$env:TENCENT_COS_BUCKET = '<你的 bucket-appid>'
$env:TENCENT_COS_HOST = '<你的 COS 访问域名>'
$env:TENCENT_COS_REGION = 'ap-nanjing'
```

将尖括号示例替换为自己的值，不要原样使用。环境变量仅在当前终端及其子进程中生效；IDE 启动和新终端需另外设置。不要把真实值提交到仓库，也不要将它们放入前端 `VITE_*` 变量。

### 3. 启动 MySQL、Redis 与 RabbitMQ

在 `ai-test-s` 目录执行：

```powershell
docker compose up -d mysql redis rabbitmq
docker compose ps
```

本指南复用 Compose 的基础服务，但后端先在本机运行，注意宿主端口与容器端口不同：

| 服务 | 本机连接地址 | 容器内部端口 |
| --- | --- | --- |
| MySQL | localhost:3307 | 3306 |
| Redis | localhost:6380 | 6379 |
| RabbitMQ AMQP | localhost:5673 | 5672 |
| RabbitMQ 管理台 | http://localhost:15673 | 15672 |

MySQL 在**空数据卷首次启动**时自动执行 `sql/create_table.sql`，创建 `ai_eval` 和演示数据。等待数据库初始化完成，再启动后端：

```powershell
docker compose logs mysql
```

已有数据卷不会自动重新执行初始化脚本。需要手动导入时，使用数据库客户端执行该 SQL；不要为了重新初始化直接删除有用的数据卷。

### 4. 安装 RabbitMQ 延时交换机插件

从 [官方插件 Releases](https://github.com/rabbitmq/rabbitmq-delayed-message-exchange/releases) 下载与 **RabbitMQ 3.12** 匹配的 `.ez` 文件，不要直接选不兼容的最新版本。将下方路径替换为实际下载文件：

```powershell
docker cp "C:/Downloads/rabbitmq_delayed_message_exchange-<匹配版本>.ez" rabbitmq:/plugins/
docker compose exec rabbitmq rabbitmq-plugins enable rabbitmq_delayed_message_exchange
docker compose restart rabbitmq
docker compose exec rabbitmq rabbitmq-plugins list -e
```

确认插件已启用且 RabbitMQ 就绪。上述复制方式适合本地试运行，容器重建后需重新安装；长期部署应使用预装兼容插件的自定义镜像。

### 5. 配置本地后端连接

继续在设置过密钥的同一终端中执行：

```powershell
$env:SPRING_PROFILES_ACTIVE = 'local'
$env:SPRING_DATASOURCE_URL = 'jdbc:mysql://localhost:3307/ai_eval?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai'
$env:SPRING_DATA_REDIS_HOST = 'localhost'
$env:SPRING_DATA_REDIS_PORT = '6380'
$env:SPRING_DATA_REDIS_DATABASE = '1'
$env:SPRING_RABBITMQ_HOST = 'localhost'
$env:SPRING_RABBITMQ_PORT = '5673'
```

基础配置中的 Redis `database` 未填写，这里明确设为 `1`。Compose 中的 Redis 默认未开启密码，不要给这一实例额外设置错误的 `REDIS_PASSWORD`。若使用自己的 Redis，请同步配置实际密码。

也可创建已被忽略的 `src/main/resources/application-local.yml` 保存本机配置，但不得提交真实凭据。若该文件已存在，应检查其中是否有旧值覆盖当前配置。

### 6. 首次同步模型列表

SQL 没有预置模型列表，定时任务默认每天凌晨 2 点执行，不会自动在首次启动时同步。仓库已有集成测试调用同步方法：

```powershell
mvn '-Dtest=ModelControllerTest#getAllModels' test
```

该测试需要 MySQL、Redis、RabbitMQ、COS 配置和外网连接，不是隔离单元测试；其中查询使用了演示用户 ID `3`，适用于初次导入的演示数据库。同步方法内部会捕获异常，因此**测试通过不等于同步成功**，应同时检查日志、数据库 `model` 表和前端模型列表。

### 7. 启动后端

```powershell
mvn spring-boot:run
```

也可打包后运行：

```powershell
mvn clean package -DskipTests
java -jar target/springboot-init-0.0.1-SNAPSHOT.jar --spring.profiles.active=local
```

后端默认地址为 `http://localhost:8123/api`：

- 健康检查：`http://localhost:8123/api/health/`
- 接口文档：`http://localhost:8123/api/doc.html`
- Actuator 健康检查：`http://localhost:8123/api/actuator/health`

### 8. 启动前端

新开终端，在克隆仓库的 `ai-test-s/frontend` 目录执行：

```powershell
npm ci
npm run dev
```

打开终端显示的地址，通常为 `http://localhost:5173`。开发环境使用 `/api`，由 Vite 代理到本机 `8123` 端口，不需要将第三方 API Key 放入浏览器。

前端检查与构建：

```powershell
npm run type-check
npm run build
```

### 9. 完成一次端到端验证

1. 打开前端，注册并登录自己的账号。
2. 确认模型列表已同步，选择当前账号可调用的模型发送消息，检查流式输出及历史对话。
3. 进入 Prompt Lab，使用同一模型对比多个提示词变体。
4. 创建自己的评测场景与少量测试用例，提交批量任务，观察任务进度。
5. 查看评分、图表与报告导出，再检查用量及成本统计。
6. 配置好 COS 后测试图片上传；图片生成和联网搜索还取决于所选模型能力与服务权限。

初始化 SQL 含 `admin`、`user`、`test` 演示账号，初始密码见 SQL 注释。它们仅用于本地演示，**公网部署前必须修改或移除演示账号，并关闭未受保护的基础服务端口**。部分演示数据带有固定用户 ID，建议使用自己的账号创建场景，避免把演示数据可见性误判为功能故障。

## Docker 部署

仓库提供前后端多阶段 Dockerfile 与 Compose，但完整容器部署前还需完成以下适配，不能只执行启动命令就忽略依赖。

1. 准备上文环境变量，并为 RabbitMQ 使用预装兼容延时插件的镜像，或先启动基础服务安装插件。
2. 修改 `ai-test-s/frontend/nginx.conf` 中两处 `host.docker.internal:8123` 为 Compose 服务地址 `backend:8123`，使 Nginx 在容器网络内访问后端。现有配置偏向 Docker Desktop 的宿主机访问方式，不应原样当作通用 Linux 部署配置。
3. SSE 代理建议关闭缓冲并设置适当的读取超时；WebSocket 需保留 Upgrade / Connection 请求头配置。
4. 确认 COS 地域、桶与访问域名相互匹配。Compose 中外部密钥应在宿主环境或部署平台中注入。
5. 如果前一步为了本地启动设置过 `SPRING_*` 环境变量，注意 Compose 已给后端声明内部服务地址；容器不能使用宿主端口连接 MySQL / Redis / RabbitMQ。

完成适配后，在 `ai-test-s` 执行：

```powershell
docker compose up -d mysql redis rabbitmq
# 确认数据库初始化完成、RabbitMQ 插件已启用，再启动应用
docker compose up -d --build backend frontend
docker compose logs -f backend
```

- 前端：`http://localhost:8090`
- 后端：`http://localhost:8123/api`
- 首次模型同步：在配置正确的本机执行上文集成测试，连接映射后的数据库；或在开发环境调用 `SyncModelJob.syncModels()`。当前没有公开的模型同步 HTTP 接口。
- `depends_on` 不代表依赖服务已经就绪，启动异常时先检查数据库、消息队列与插件状态。
- 对外部署还需要域名、HTTPS、访问控制、数据备份与凭据管理；不要直接将本地演示配置暴露到公网。

## 常见问题与当前边界

- **启动时报 unknown exchange type `x-delayed-message`**：RabbitMQ 延时插件没有安装或版本不匹配。即使笔记讨论过撤回任务重试，当前交换机声明仍依赖该插件。
- **COS 初始化或上传失败**：检查密钥、地域、桶权限与域名；当前代码未实现自动关闭 COS 的配置开关。
- **模型列表为空**：检查首次同步、OpenRouter 连通性与授权；不要只根据测试退出状态判断同步成功。
- **模型调用失败**：默认模型标识可能随供应商变化，需选择当前有效且有权限的模型。免费额度、限流和可用性均由服务商决定。
- **流式输出延迟成批出现**：检查 Nginx / 上游代理缓冲、读取超时和客户端连接释放。
- **批量任务重试**：笔记记录了进度并发一致性问题及撤回重试的过程，不将可靠重试与严格一次执行视为已完成能力。
- **预算实时性**：成本数据更新与查询不等于服务端主动推送预算告警；成本估算也不替代供应商账单。
- **自动化验证**：现有测试依赖外部服务，不是开箱即跑的无依赖测试集；本文提供复现路径，不代表所有环境下均已完成验证。
