package cn.hgy.ai.domain.session.adapter.repository;

import cn.hgy.ai.domain.session.model.valobj.gateway.McpGatewayConfigVO;
import cn.hgy.ai.domain.session.model.valobj.gateway.McpGatewayProtocolConfigVO;
import cn.hgy.ai.domain.session.model.valobj.gateway.McpGatewayToolConfigVO;

import java.util.List;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-02-23
 * @Description: 会话仓储接口
 */
public interface ISessionRepository {
    McpGatewayConfigVO queryMcpGatewayConfigByGatewayId(String gatewayId);

    List<McpGatewayToolConfigVO> queryMcpGatewayToolConfigListByGatewayId(String gatewayId);

    McpGatewayProtocolConfigVO queryMcpGatewayProtocolConfig(String gatewayId);
}
