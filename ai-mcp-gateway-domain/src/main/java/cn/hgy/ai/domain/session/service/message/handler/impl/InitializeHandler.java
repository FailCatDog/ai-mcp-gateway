package cn.hgy.ai.domain.session.service.message.handler.impl;

import cn.hgy.ai.domain.session.adapter.repository.ISessionRepository;
import cn.hgy.ai.domain.session.model.valobj.McpSchemaVO;
import cn.hgy.ai.domain.session.model.valobj.gateway.McpGatewayConfigVO;
import cn.hgy.ai.domain.session.service.message.handler.IRequestHandler;
import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-01-30
 * @Description: 协议握手，建立客户端与服务器的连接
 */
@Slf4j
@Service("initializeHandler")
@RequiredArgsConstructor
public class InitializeHandler implements IRequestHandler {

    private final ISessionRepository repository;

    /**
     * 对照 io.modelcontextprotocol.spec.McpServerSession
     * <br/>
     * McpServerSession.handle -> McpSchema.JSONRPCRequest -> handleIncomingRequest
     * -> McpSchema.METHOD_INITIALIZE -> McpAsyncServer.asyncInitializeRequestHandler
     * -> result -> new McpSchema.JSONRPCResponse(McpSchema.JSONRPC_VERSION, request.id(), result, null)
     * <br/>
     * {
     * "id": "a355a5f7-0",
     * "jsonrpc": "2.0",
     * "result": {
     * "capabilities": {
     * "completions": {},
     * "logging": {},
     * "prompts": {
     * "listChanged": true
     * },
     * "resources": {
     * "listChanged": true,
     * "subscribe": false
     * },
     * "tools": {
     * "listChanged": true
     * }
     * },
     * "instructions": "This server provides weather information tools and resources",
     * "protocolVersion": "2024-11-05",
     * "serverInfo": {
     * "name": "ai-mcp-gateway-demo-mcp-server-test",
     * "version": "1.0.0"
     * }
     * }
     * }
     */
    @Override
    public McpSchemaVO.JSONRPCResponse handle(String gatewayId, McpSchemaVO.JSONRPCRequest message) {
        log.info("消息处理服务-initialize gatewayId:{} request.params:{}", gatewayId, JSON.toJSONString(message.params()));

        // 1. 反序列化客户端 initialize 请求参数，提取协议版本和客户端信息
        McpSchemaVO.InitializeRequest initializeRequest = McpSchemaVO.unmarshalFrom(message.params(), new TypeReference<>() {
        });

        // 2. 查询网关配置（网关名称、版本号、描述等），用于构建 serverInfo
        McpGatewayConfigVO mcpGatewayConfigVO = repository.queryMcpGatewayConfigByGatewayId(gatewayId);

        // 3. 按 MCP 协议规范构建 InitializeResult
        //    - protocolVersion: 回显客户端请求的协议版本，确保双端协议一致
        //    - ServerCapabilities: 声明本网关支持的能力集（tools/resources/prompts/completions 等）
        //    - serverInfo: 网关标识信息，供客户端记录和展示
        //    - instructions: 网关描述，作为 AI 的系统提示
        McpSchemaVO.InitializeResult initializeResult = new McpSchemaVO.InitializeResult(initializeRequest.protocolVersion(),
                new McpSchemaVO.ServerCapabilities(new McpSchemaVO.ServerCapabilities.CompletionCapabilities(),
                        new HashMap<>(),
                        new McpSchemaVO.ServerCapabilities.LoggingCapabilities(),
                        new McpSchemaVO.ServerCapabilities.PromptCapabilities(true),
                        new McpSchemaVO.ServerCapabilities.ResourceCapabilities(false, true),
                        new McpSchemaVO.ServerCapabilities.ToolCapabilities(true)),
                new McpSchemaVO.Implementation(mcpGatewayConfigVO.getGatewayName(), mcpGatewayConfigVO.getVersion()),
                mcpGatewayConfigVO.getGatewayDesc()
        );

        // 4. 封装为标准 JSON-RPC 2.0 响应并返回，通过 SSE 推送给客户端
        return new McpSchemaVO.JSONRPCResponse(McpSchemaVO.JSONRPC_VERSION, message.id(), initializeResult, null);
    }
}
