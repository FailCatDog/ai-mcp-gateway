package cn.hgy.ai.domain.auth.service.register;

import cn.hgy.ai.domain.auth.adapter.repository.IAuthRepository;
import cn.hgy.ai.domain.auth.model.entity.RegisterCommandEntity;
import cn.hgy.ai.domain.auth.model.valobj.McpGatewayAuthVO;
import cn.hgy.ai.domain.auth.model.valobj.enums.AuthStatusEnum;
import cn.hgy.ai.domain.auth.service.IAuthRegisterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-03-05
 * @Description: 权限注册服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthRegisterService implements IAuthRegisterService {

    private final IAuthRepository repository;

    @Override
    public String register(RegisterCommandEntity commandEntity) {
        // 1. 生成 API Key | gw 网关缩写，方便区分
        String apiKey = "gw-" + RandomStringUtils.randomAlphanumeric(48);

        // 2. 构建聚合对象
        McpGatewayAuthVO mcpGatewayAuthVO = McpGatewayAuthVO.builder()
                .gatewayId(commandEntity.getGatewayId())
                .apiKey(apiKey)
                .rateLimit(commandEntity.getRateLimit())
                .expireTime(commandEntity.getExpireTime())
                .status(AuthStatusEnum.AuthConfig.ENABLE)
                .build();

        // 3. 保存数据
        repository.insert(mcpGatewayAuthVO);

        // 4. 返回结果
        return apiKey;
    }
}
