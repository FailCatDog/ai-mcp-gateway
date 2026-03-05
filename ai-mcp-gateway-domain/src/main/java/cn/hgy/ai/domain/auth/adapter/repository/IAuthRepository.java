package cn.hgy.ai.domain.auth.adapter.repository;

import cn.hgy.ai.domain.auth.model.entity.LicenseCommandEntity;
import cn.hgy.ai.domain.auth.model.valobj.McpGatewayAuthVO;
import cn.hgy.ai.domain.auth.model.valobj.enums.AuthStatusEnum;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-03-05
 * @Description:
 */
public interface IAuthRepository {
    int queryEffectiveGatewayAuthCount(String gatewayId);

    McpGatewayAuthVO queryEffectiveGatewayAuthInfo(LicenseCommandEntity licenseCommandEntity);

    void insert(McpGatewayAuthVO mcpGatewayAuthVO);

    AuthStatusEnum.GatewayConfig queryGatewayAuthStatus(String gatewayId);
}
