package cn.hgy.ai.domain.session.adapter.repository;

import cn.hgy.ai.domain.session.model.valobj.gateway.McpGatewayConfigVO;
import cn.hgy.ai.domain.session.model.valobj.gateway.McpToolProtocolConfigVO;
import cn.hgy.ai.domain.session.model.valobj.gateway.McpToolConfigVO;

import java.util.List;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-02-23
 * @Description: 会话仓储接口
 */
public interface ISessionRepository {
    McpGatewayConfigVO queryMcpGatewayConfigByGatewayId(String gatewayId);

    List<McpToolConfigVO> queryMcpGatewayToolConfigListByGatewayId(String gatewayId);

    McpToolProtocolConfigVO queryMcpGatewayProtocolConfig(String gatewayId, String toolName);
}
