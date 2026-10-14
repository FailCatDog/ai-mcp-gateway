package cn.hgy.ai.domain.session.service.message.handler.impl;

import cn.hgy.ai.domain.session.model.valobj.McpSchemaVO;
import cn.hgy.ai.domain.session.service.message.handler.IRequestHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-01-30
 * @Description: 返回可用资源列表
 */
@Slf4j
@Service("resourcesListHandler")
public class ResourceListHandler implements IRequestHandler {
    @Override
    public McpSchemaVO.JSONRPCResponse handle(String gatewayId, McpSchemaVO.JSONRPCRequest message) {
        return new McpSchemaVO.JSONRPCResponse("2.0", message.id(), Map.of(
                "resources", Map.of(
                        "resources", new Object[]{}
                )
        ), null);
    }
}

