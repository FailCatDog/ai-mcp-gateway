package cn.hgy.ai.domain.session.service.message.handler.impl;

import cn.hgy.ai.domain.session.model.valobj.McpSchemaVO;
import cn.hgy.ai.domain.session.service.message.handler.IRequestHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-01-30
 * @Description: 返回工具
 */
@Slf4j
@Service("toolsListHandler")
public class ToolsListHandler implements IRequestHandler {
    @Override
    public McpSchemaVO.JSONRPCResponse handle(String gatewayId, McpSchemaVO.JSONRPCRequest message) {

        return new McpSchemaVO.JSONRPCResponse("2.0", message.id(), Map.of("tools", new Object[]{
                Map.of("name", "toUpperCase",
                        "description", "小写转换大写",
                        "inputSchema", Map.of("type", "object",
                                "properties", Map.of(
                                        "word", Map.of(
                                        "type", "string",
                                        "description", "单词，字符串"
                                )))
                        )
        }), null);
    }
}

