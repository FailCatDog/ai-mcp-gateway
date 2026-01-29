package cn.hgy.ai.domain.session.service.management.handler.impl;

import cn.hgy.ai.domain.session.model.valobj.McpSchemaVO;
import cn.hgy.ai.domain.session.service.management.handler.IRequestHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
        return null;
    }
}
