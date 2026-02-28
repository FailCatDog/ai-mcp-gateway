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
 * @Description:
 */
@Slf4j
@Service("initializeHandler")
@RequiredArgsConstructor
public class InitializeHandler implements IRequestHandler {

    private final ISessionRepository repository;

    @Override
    public McpSchemaVO.JSONRPCResponse handle(String gatewayId, McpSchemaVO.JSONRPCRequest message) {

        log.info("消息处理服务-initialize gatewayId:{} request.params:{}", gatewayId, JSON.toJSONString(message.params()));

        // 1. 转换参数
        McpSchemaVO.InitializeRequest initializeRequest = McpSchemaVO.unmarshalFrom(message.params(), new TypeReference<>() {
        });

        // 2. 查询配置
        McpGatewayConfigVO mcpGatewayConfigVO = repository.queryMcpGatewayConfigByGatewayId(gatewayId);

        // 3. 组装信息
        McpSchemaVO.InitializeResult initializeResult = new McpSchemaVO.InitializeResult(initializeRequest.protocolVersion(),
                new McpSchemaVO.ServerCapabilities(new McpSchemaVO.ServerCapabilities.CompletionCapabilities(),
                        new HashMap<>(),
                        new McpSchemaVO.ServerCapabilities.LoggingCapabilities(),
                        new McpSchemaVO.ServerCapabilities.PromptCapabilities(true),
                        new McpSchemaVO.ServerCapabilities.ResourceCapabilities(false, true),
                        new McpSchemaVO.ServerCapabilities.ToolCapabilities(true)),
                new McpSchemaVO.Implementation(mcpGatewayConfigVO.getToolName(), mcpGatewayConfigVO.getToolVersion()),
                mcpGatewayConfigVO.getToolDesc()
        );

        // 4. 返回结果
        return new McpSchemaVO.JSONRPCResponse(McpSchemaVO.JSONRPC_VERSION, message.id(), initializeResult, null);
    }
}
