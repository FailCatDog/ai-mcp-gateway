package cn.hgy.ai.domain.session.service.message.handler.impl;

import cn.hgy.ai.domain.session.adapter.port.ISessionPort;
import cn.hgy.ai.domain.session.adapter.repository.ISessionRepository;
import cn.hgy.ai.domain.session.model.valobj.McpSchemaVO;
import cn.hgy.ai.domain.session.model.valobj.gateway.McpGatewayProtocolConfigVO;
import cn.hgy.ai.domain.session.service.message.handler.IRequestHandler;
import cn.hgy.ai.types.enums.McpErrorCodes;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
* @Author: 黄光宇
* @CreateTime: 2026-01-30
* @Description:
*/
@Slf4j
@Service("toolsCalHandler")
@RequiredArgsConstructor
public class ToolsCallHandler implements IRequestHandler {

    private final ISessionRepository repository;
    private final ISessionPort port;

    @Override
    public McpSchemaVO.JSONRPCResponse handle(String gatewayId, McpSchemaVO.JSONRPCRequest message) {

        try {
            McpGatewayProtocolConfigVO mcpGatewayProtocolConfigVO = repository.queryMcpGatewayProtocolConfig(gatewayId);

            McpSchemaVO.CallToolRequest callToolRequest = McpSchemaVO.unmarshalFrom(message.params(), new TypeReference<>() {
            });

            Map<String, Object> argumentsObj = callToolRequest.arguments();

            // todo 后续详细拆分会使用？
            String name = callToolRequest.name();

            Object result = port.tollCall(mcpGatewayProtocolConfigVO.getHttpConfig(), argumentsObj);

            return new McpSchemaVO.JSONRPCResponse(McpSchemaVO.JSONRPC_VERSION, message.id(), Map.of(
                    "content", new Object[]{Map.of(
                            "type", "text",
                            "text",result
                    )},
                    "isError", "false"
            ), null);
        }catch (Exception e) {
            return new McpSchemaVO.JSONRPCResponse("2.0", message.id(), null, new  McpSchemaVO.JSONRPCResponse.JSONRPCError(McpErrorCodes.INVALID_PARAMS, e.getMessage(), null));
        }
//
//        Object id = message.id();
//        Object params = message.params();
//
//        if (!(params instanceof Map)) {
//            return new McpSchemaVO.JSONRPCResponse("2.0", message.id(), null, new  McpSchemaVO.JSONRPCResponse.JSONRPCError(McpErrorCodes.INVALID_PARAMS, "无效的方法或参数", null));
//        }
//
//        Map<String, Object> paramsMap = (Map<String, Object>) params;
//
//        String toolName = paramsMap.get("name").toString();
//        Object argumentsObj = paramsMap.get("arguments");
//
//        Map<String, Object> arguments = (Map<String, Object>) argumentsObj;
//
//        if ("toUpperCase".equals(toolName)) {
//            String word = arguments.get("word").toString();
//
//            return new McpSchemaVO.JSONRPCResponse("2.0", message.id(), Map.of(
//                    "content", new Object[]{Map.of(
//                            "type", "text",
//                            "text", word.toUpperCase()
//                    )}
//            ), null);
//        }
//
//        return null;
    }
}
