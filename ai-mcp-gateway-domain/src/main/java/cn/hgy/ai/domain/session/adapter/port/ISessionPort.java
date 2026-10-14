package cn.hgy.ai.domain.session.adapter.port;

import cn.hgy.ai.domain.session.model.valobj.gateway.McpToolProtocolConfigVO;

import java.io.IOException;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-03-04
 * @Description:
 */
public interface ISessionPort {

    Object toolCall(McpToolProtocolConfigVO.HTTPConfig httpConfig, Object params) throws IOException;

    Object aiModelCall(McpToolProtocolConfigVO.AIModelConfig aiModelConfig, Object params);
}
