package cn.hgy.ai.domain.session.model.entity;

import cn.hgy.ai.domain.session.model.valobj.McpSchemaVO;
import lombok.*;

import java.io.IOException;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-03-05
 * @Description:
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class HandleMessageCommandEntity {
    private String gatewayId;

    private String sessionId;

    private String apiKey;

    private McpSchemaVO.JSONRPCMessage jsonrpcMessage;

    public HandleMessageCommandEntity(String gatewayId, String apiKey, String sessionId, String messageBody) throws Exception {
        this.gatewayId = gatewayId;
        this.sessionId = sessionId;
        this.apiKey = apiKey;
        this.jsonrpcMessage = McpSchemaVO.deserializeJsonRpcMessage(messageBody);
    }
}
