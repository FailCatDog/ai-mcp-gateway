package cn.hgy.ai.infrastructure.adapter.repository;

import cn.hgy.ai.domain.auth.adapter.repository.IAuthRepository;
import cn.hgy.ai.domain.auth.model.entity.LicenseCommandEntity;
import cn.hgy.ai.domain.auth.model.valobj.McpGatewayAuthVO;
import cn.hgy.ai.domain.auth.model.valobj.enums.AuthStatusEnum;
import cn.hgy.ai.infrastructure.dao.IMcpGatewayAuthDao;
import cn.hgy.ai.infrastructure.dao.IMcpGatewayDao;
import cn.hgy.ai.infrastructure.dao.po.McpGatewayAuthPO;
import cn.hgy.ai.infrastructure.dao.po.McpGatewayPO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-03-05
 * @Description: 鉴权仓储服务
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class AuthRepository implements IAuthRepository {

    private final IMcpGatewayAuthDao mcpGatewayAuthDao;

    private final IMcpGatewayDao mcpGatewayDao;

    @Override
    public int queryEffectiveGatewayAuthCount(String gatewayId) {
        return mcpGatewayAuthDao.queryEffectiveGatewayAuthCount(gatewayId);
    }

    @Override
    public McpGatewayAuthVO queryEffectiveGatewayAuthInfo(LicenseCommandEntity commandEntity) {

        McpGatewayAuthPO poReq = new McpGatewayAuthPO();
        poReq.setGatewayId(commandEntity.getGatewayId());
        poReq.setApiKey(commandEntity.getApiKey());

        McpGatewayAuthPO mcpGatewayAuthPO = mcpGatewayAuthDao.queryMcpGatewayAuthPO(poReq);
        if (null == mcpGatewayAuthPO) return null;

        return McpGatewayAuthVO.builder()
                .gatewayId(mcpGatewayAuthPO.getGatewayId())
                .apiKey(mcpGatewayAuthPO.getApiKey())
                .rateLimit(mcpGatewayAuthPO.getRateLimit())
                .expireTime(mcpGatewayAuthPO.getExpireTime())
                .status(AuthStatusEnum.AuthConfig.get(mcpGatewayAuthPO.getStatus()))
                .build();
    }

    @Override
    public void insert(McpGatewayAuthVO mcpGatewayAuthVO) {
        McpGatewayAuthPO mcpGatewayAuthPO = McpGatewayAuthPO.builder()
                .gatewayId(mcpGatewayAuthVO.getGatewayId())
                .apiKey(mcpGatewayAuthVO.getApiKey())
                .rateLimit(mcpGatewayAuthVO.getRateLimit())
                .expireTime(mcpGatewayAuthVO.getExpireTime())
                .status(mcpGatewayAuthVO.getStatus().getCode())
                .build();
        mcpGatewayAuthDao.insert(mcpGatewayAuthPO);
    }

    @Override
    public AuthStatusEnum.GatewayConfig queryGatewayAuthStatus(String gatewayId) {
        McpGatewayPO mcpGatewayPO = mcpGatewayDao.queryMcpGatewayByGatewayId(gatewayId);
        return AuthStatusEnum.GatewayConfig.get(mcpGatewayPO.getAuth());
    }
}
