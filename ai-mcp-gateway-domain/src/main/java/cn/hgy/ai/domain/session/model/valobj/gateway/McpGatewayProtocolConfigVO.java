package cn.hgy.ai.domain.session.model.valobj.gateway;

import lombok.*;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-03-04
 * @Description:
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class McpGatewayProtocolConfigVO {

    private HTTPConfig httpConfig;

    @Data
    public static class HTTPConfig {
        private String httpUrl;
        private String httpHeaders;
        private String httpMethod;
        private Integer timeout;
    }
}
