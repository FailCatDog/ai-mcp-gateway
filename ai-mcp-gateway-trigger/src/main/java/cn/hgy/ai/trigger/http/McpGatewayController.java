package cn.hgy.ai.trigger.http;

import cn.hgy.ai.api.IMcpGatewayService;
import cn.hgy.ai.cases.mcp.IMcpSessionService;
import cn.hgy.ai.domain.session.model.valobj.McpSchemaVO;
import cn.hgy.ai.domain.session.model.valobj.SessionConfigVO;
import cn.hgy.ai.domain.session.service.ISessionManagementService;
import cn.hgy.ai.domain.session.service.ISessionMessageService;
import cn.hgy.ai.types.enums.ResponseCode;
import cn.hgy.ai.types.exception.AppException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


/**
 * @Author: 黄光宇
 * @CreateTime: 2026-01-29
 * @Description:
 */
@Slf4j
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
@RequestMapping("/")
@RequiredArgsConstructor
public class McpGatewayController implements IMcpGatewayService {

    private final IMcpSessionService sessionService;
    private final ISessionMessageService sessionMessageService;
    private final ISessionManagementService sessionManagementService;
    private final ObjectMapper objectMapper;

    @GetMapping(value = "{gatewayId}/mcp/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Override
    public Flux<ServerSentEvent<String>> establishSSEConnection(@PathVariable("gatewayId") String gatewayId) throws Exception {
        try {
            log.info("建立 MCP 连接 {}", gatewayId);

            if (StringUtils.isBlank(gatewayId)) {
                log.info("网关ID为空");
                throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
            }


            return sessionService.createMcpSession(gatewayId);
        } catch (Exception e) {
            log.info("建立 MCP 连接失败 {}", gatewayId);
            throw e;
        }
    }

    @PostMapping(value = "{gatewayId}/mcp/sse", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public Mono<ResponseEntity<Object>> handleMessage(@PathVariable("gatewayId") String getawayId,
                                                      @RequestParam String sessionId,
                                                      @RequestBody String messageBody) {
        try {

            log.info("处理 MCP SSE 消息, gatewayId:{} sessionId:{} messageBody:{}", getawayId, sessionId, messageBody);

            SessionConfigVO session = sessionManagementService.getSession(sessionId);
            if (null == session) {
                return Mono.just(ResponseEntity.notFound().build());
            }

            McpSchemaVO.JSONRPCMessage jsonrpcMessage = McpSchemaVO.deserializeJsonMessage(messageBody);
            log.info("序列化消息 {}", jsonrpcMessage.jsonrpc());

            // todo 暂时直接调用domain后续调整
            McpSchemaVO.JSONRPCResponse jsonrpcResponse = sessionMessageService.processHandlerMessage(getawayId, jsonrpcMessage);

            if (null != jsonrpcResponse) {
                String responseJson = objectMapper.writeValueAsString(jsonrpcMessage);

                session.getSink().tryEmitNext(ServerSentEvent.<String>builder()
                        .event("message")
                        .data(responseJson)
                        .build());
            }

            return Mono.just(ResponseEntity.accepted().build());
        } catch (Exception e) {
            return Mono.just(ResponseEntity.internalServerError().build());
        }
    }
}
