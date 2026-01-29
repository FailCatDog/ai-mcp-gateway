package cn.hgy.ai.trigger.http;

import cn.hgy.ai.api.IMcpGatewayService;
import cn.hgy.ai.cases.mcp.IMcpSessionService;
import cn.hgy.ai.domain.session.service.ISessionManagementService;
import cn.hgy.ai.types.enums.ResponseCode;
import cn.hgy.ai.types.exception.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

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

            return Mono.just(ResponseEntity.ok(Map.of("status", "sent via sse")));
        } catch (Exception e) {
            return Mono.empty();
        }
    }
}
