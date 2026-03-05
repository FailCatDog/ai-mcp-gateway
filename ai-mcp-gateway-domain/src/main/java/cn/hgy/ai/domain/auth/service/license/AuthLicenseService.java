package cn.hgy.ai.domain.auth.service.license;

import cn.hgy.ai.domain.auth.adapter.repository.IAuthRepository;
import cn.hgy.ai.domain.auth.model.entity.LicenseCommandEntity;
import cn.hgy.ai.domain.auth.model.valobj.McpGatewayAuthVO;
import cn.hgy.ai.domain.auth.model.valobj.enums.AuthStatusEnum;
import cn.hgy.ai.domain.auth.service.IAuthLicenseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-03-05
 * @Description:
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthLicenseService implements IAuthLicenseService {

    private final IAuthRepository repository;

    @Override
    public boolean checkLicense(LicenseCommandEntity commandEntity) {

        AuthStatusEnum.GatewayConfig gatewayAuthStatus = repository.queryGatewayAuthStatus(commandEntity.getGatewayId());

        if (AuthStatusEnum.GatewayConfig.NOT_VERIFIED.equals(gatewayAuthStatus)) return true;

        McpGatewayAuthVO mcpGatewayAuthVO = repository.queryEffectiveGatewayAuthInfo(commandEntity);

        if (null == mcpGatewayAuthVO) return false;

        if (AuthStatusEnum.AuthConfig.DISABLE.equals(mcpGatewayAuthVO.getStatus())) {
            return false;
        }

        Date expireTime = mcpGatewayAuthVO.getExpireTime();
        if (null == expireTime) return true;

        return new Date().before(expireTime);
    }
}
