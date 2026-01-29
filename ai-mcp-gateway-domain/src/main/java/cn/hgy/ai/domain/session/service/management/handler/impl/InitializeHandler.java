package cn.hgy.ai.domain.session.service.management.handler.impl;

import cn.hgy.ai.domain.session.model.valobj.McpSchemaVO;
import cn.hgy.ai.domain.session.service.management.handler.IRequestHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-01-30
 * @Description:
 */
@Slf4j
@Service("initializeHandler")
public class InitializeHandler implements IRequestHandler {

    @Override
    public McpSchemaVO.JSONRPCResponse handle(McpSchemaVO.JSONRPCRequest message) {

        log.info("模拟初始化请求");

        return new McpSchemaVO.JSONRPCResponse("2.0", message.id(), Map.of(
                "protocolVersion", "2024-11-05",
                "capabilities", Map.of(
                        "tools", Map.of(),
                        "resources", Map.of()
                ),
                "serverInfo", Map.of(
                        "name", "MCP Weather Proxy Server",
                        "version", "1.0.0"
                )
        ), null);

    }
}
