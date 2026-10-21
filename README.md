# AI MCP Gateway
基于 Spring Boot 与 Model Context Protocol（MCP）构建的工具网关服务。项目可将已注册的 HTTP 接口及其参数映射为 MCP 工具，供 MCP 客户端通过 JSON-RPC 调用，并支持 SSE 会话、API Key 校验和调用频率限制。
## 功能概览
- 通过 MCP SSE 建立客户端会话，并处理 JSON-RPC 消息。
- 从数据库读取网关、认证、工具注册及协议映射配置。
- 将 MCP 工具参数映射到 HTTP 请求，并将 HTTP 响应转换为 MCP 结果。
- 支持网关 API Key、有效期和速率限制等认证配置。
- 集成 Spring AI 模型相关依赖，并提供 OpenAI 兼容服务配置入口。
## 技术栈
- Java 17
- Spring Boot 3.4.3
- Spring AI 1.0.0
- Maven 多模块
- MySQL 8、MyBatis
- Reactor、Spring WebFlux / SSE
## 模块结构
| 模块 | 职责 |
| --- | --- |
| `ai-mcp-gateway-app` | Spring Boot 启动入口、应用配置及运行资源 |
| `ai-mcp-gateway-trigger` | HTTP 等外部触发入口 |
| `ai-mcp-gateway-case` | MCP 会话、消息用例编排 |
| `ai-mcp-gateway-domain` | 领域模型、领域服务及仓储接口 |
| `ai-mcp-gateway-infrastructure` | 数据访问、仓储实现及外部适配器 |
| `ai-mcp-gateway-api` | 对外服务接口及响应模型 |
| `ai-mcp-gateway-types` | 公共类型、常量及异常定义 |
## 环境要求
- JDK 17
- Maven 3.8 或更高版本
- MySQL 8.x
- 可选：可访问的 OpenAI 兼容模型服务及有效 API Key
## 快速开始
### 1. 初始化数据库
执行数据库脚本：
```text
docs/dev-ops/mysql/sql/ai_mcp_gateway.sql
```
脚本创建 `ai_mcp_gateway` 数据库及网关、认证、工具注册和协议映射等表，并包含用于验证流程的示例数据。部署到非本地或正式环境前，请检查并替换脚本中的示例密钥与示例配置。
### 2. 配置运行参数
默认配置文件 `ai-mcp-gateway-app/src/main/resources/application.yml` 使用 `dev` 配置。按实际环境修改 `application-dev.yml` 中的数据库连接及模型服务参数，重点配置：
- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`
- `spring.ai.openai.api-key`
- `spring.ai.openai.base-url`
- `spring.ai.openai.chat.options.model`
当前开发配置的数据库默认地址为 `127.0.0.1:13306`，数据库名为 `ai_mcp_gateway_v2`；而仓库 SQL 脚本创建的数据库名为 `ai_mcp_gateway`。请将两者配置为一致后再启动。不要将真实密钥或生产密码提交到版本库。
### 3. 构建
在项目根目录执行：
```bash
mvn clean package
```
应用构建产物位于 `ai-mcp-gateway-app/target/ai-mcp-gateway-app.jar`。
### 4. 启动
```bash
java -jar ai-mcp-gateway-app/target/ai-mcp-gateway-app.jar
```
默认开发配置端口为 `8091`，Web 上下文路径为 `/api-gateway`。也可以使用 Maven 启动：
```bash
mvn -pl ai-mcp-gateway-app -am spring-boot:run
```
可通过 Spring 配置切换运行环境，例如：
```bash
java -jar ai-mcp-gateway-app/target/ai-mcp-gateway-app.jar --spring.profiles.active=dev
```
## MCP 接口
默认基础地址：`http://localhost:8091/api-gateway`。
### 建立 SSE 会话
```http
GET /{gatewayId}/mcp/sse?apiKey={apiKey}
Accept: text/event-stream
```
例如数据库示例配置使用网关 ID `gateway_001`。连接成功后，服务端通过 SSE 推送会话相关事件及 MCP 响应。
### 发送 JSON-RPC 消息
```http
POST /{gatewayId}/mcp/sse?sessionId={sessionId}&apiKey={apiKey}
Content-Type: application/json
```
请求体为 MCP JSON-RPC 消息。`sessionId` 由 SSE 会话流程提供；消息通过异步处理后经 SSE 流返回。具体支持的方法以当前服务端处理器实现为准，包含初始化、工具列表及工具调用等流程。
## 工具配置说明
工具定义及 HTTP 协议映射存储在数据库中。`mcp_protocol_registry` 记录网关、工具名称、目标 HTTP 地址、请求方法、请求头、超时和重试等信息；`mcp_protocol_mapping` 描述 MCP 参数与 HTTP 请求/响应字段之间的映射关系。`mcp_gateway` 与 `mcp_gateway_auth` 分别维护网关状态及访问凭据、限流和有效期。
修改工具配置前，请确保 `gateway_id`、`tool_id` 及映射配置相互匹配，并确认目标 HTTP 服务可由网关运行环境访问。
## 测试
项目测试位于各模块的 `src/test` 目录。可执行：
```bash
mvn test
```
注意：`ai-mcp-gateway-app` 的 Maven Surefire 配置当前设置为跳过测试，因此该模块测试可能不会被执行；如需执行，请先检查并调整该模块构建配置。
## 部署资源
`docs/dev-ops/` 下提供 Docker Compose、MySQL 初始化脚本及应用启动/停止脚本。部署时请先核对对应环境文件中的端口、数据库、环境变量和挂载路径，并避免直接沿用开发环境凭据。
## 许可证
本项目根 `pom.xml` 声明使用 Apache License 2.0，详见 [LICENSE](LICENSE)。
