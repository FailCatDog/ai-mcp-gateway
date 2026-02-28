package cn.hgy.ai.infrastructure.adapter.repository;

import cn.hgy.ai.domain.session.adapter.repository.ISessionRepository;
import cn.hgy.ai.domain.session.model.valobj.gateway.McpGatewayConfigVO;
import cn.hgy.ai.infrastructure.dao.IMcpGatewayDao;
import cn.hgy.ai.infrastructure.dao.IMcpProtocolRegistryDao;
import cn.hgy.ai.infrastructure.dao.po.McpGatewayPO;
import cn.hgy.ai.infrastructure.dao.po.McpProtocolRegistryPO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-02-23
 * @Description:
 */
@Repository
@Slf4j
@RequiredArgsConstructor
public class SessionRepository implements ISessionRepository {

    private final IMcpGatewayDao mcpGatewayDao;
    private final IMcpProtocolRegistryDao mcpProtocolRegistryDao;

    @Override
    public McpGatewayConfigVO queryMcpGatewayConfigByGatewayId(String gatewayId) {
        McpGatewayPO mcpGatewayPO = mcpGatewayDao.queryMcpGatewayByGatewayId(gatewayId);
        if (null == mcpGatewayPO) return null;

        McpProtocolRegistryPO mcpProtocolRegistryPO = mcpProtocolRegistryDao.queryMcpProtocolRegistryByGatewayId(gatewayId);
        if (null == mcpProtocolRegistryPO) return null;

        return McpGatewayConfigVO.builder()
                .gatewayId(mcpGatewayPO.getGatewayId())
                .gatewayName(mcpGatewayPO.getGatewayName())
                .toolId(mcpProtocolRegistryPO.getToolId())
                .toolName(mcpProtocolRegistryPO.getToolName())
                .toolVersion(mcpProtocolRegistryPO.getToolVersion())
                .toolDesc(mcpProtocolRegistryPO.getToolDescription())
                .build();

    }
}
