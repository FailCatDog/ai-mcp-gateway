package cn.hgy.ai.domain.auth.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-03-05
 * @Description: 校验证书命令实体对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LicenseCommandEntity {

    /**
     * 网关ID
     */
    private String gatewayId;

    /**
     * API密钥
     */
    private String apiKey;
}
