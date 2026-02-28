package cn.hgy.ai.domain.session.service.message.handler;

import cn.hgy.ai.domain.session.model.valobj.McpSchemaVO;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-01-30
 * @Description:
 */
public interface IRequestHandler {
    McpSchemaVO.JSONRPCResponse handle(String gatewayId, McpSchemaVO.JSONRPCRequest message);
}
