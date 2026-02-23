package cn.hgy.ai.domain.session.service.message.handler.impl;

import cn.hgy.ai.domain.session.model.valobj.McpSchemaVO;
import cn.hgy.ai.domain.session.service.message.handler.IRequestHandler;
import cn.hgy.ai.types.enums.McpErrorCodes;
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
public class ToolsCallHandler implements IRequestHandler {
    @Override
    public McpSchemaVO.JSONRPCResponse handle(McpSchemaVO.JSONRPCRequest message) {

        Object id = message.id();
        Object params = message.params();

        if (!(params instanceof Map)) {
            return new McpSchemaVO.JSONRPCResponse("2.0", message.id(), null, new  McpSchemaVO.JSONRPCResponse.JSONRPCError(McpErrorCodes.INVALID_PARAMS, "无效的方法或参数", null));
        }

        Map<String, Object> paramsMap = (Map<String, Object>) params;

        String toolName = paramsMap.get("name").toString();
        Object argumentsObj = paramsMap.get("arguments");

        Map<String, Object> arguments = (Map<String, Object>) argumentsObj;

        if ("toUpperCase".equals(toolName)) {
            String word = arguments.get("word").toString();

            return new McpSchemaVO.JSONRPCResponse("2.0", message.id(), Map.of(
                    "content", new Object[]{Map.of(
                            "type", "text",
                            "text", word.toUpperCase()
                    )}
            ), null);
        }

        return null;
    }
}
