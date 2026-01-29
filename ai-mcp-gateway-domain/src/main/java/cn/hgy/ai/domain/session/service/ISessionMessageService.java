package cn.hgy.ai.domain.session.service;

import cn.hgy.ai.domain.session.model.valobj.McpSchemaVO;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-01-29
 * @Description:
 */
public interface ISessionMessageService {

    McpSchemaVO.JSONRPCResponse processHandlerMessage(McpSchemaVO.JSONRPCRequest message);
}
