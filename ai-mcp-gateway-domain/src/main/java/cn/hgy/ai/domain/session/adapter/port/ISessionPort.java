package cn.hgy.ai.domain.session.adapter.port;

import cn.hgy.ai.domain.session.model.valobj.gateway.McpGatewayProtocolConfigVO;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-03-04
 * @Description:
 */
public interface ISessionPort {

    Object tollCall(McpGatewayProtocolConfigVO.HTTPConfig httpConfig, Object params) throws Exception;
}
