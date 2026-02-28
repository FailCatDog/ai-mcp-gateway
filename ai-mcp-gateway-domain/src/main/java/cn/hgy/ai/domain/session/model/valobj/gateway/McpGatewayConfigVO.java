package cn.hgy.ai.domain.session.model.valobj.gateway;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-02-23
 * @Description:
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class McpGatewayConfigVO {

    private String gatewayId;
    private String gatewayName;
    private Long toolId;
    private String toolName;
    private String toolDesc;
    private String toolVersion;
}
