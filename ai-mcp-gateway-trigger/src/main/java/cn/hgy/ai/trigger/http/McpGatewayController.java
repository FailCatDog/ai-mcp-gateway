package cn.hgy.ai.trigger.http;

import cn.hgy.ai.api.IMcpGatewayService;
import cn.hgy.ai.api.response.Response;
import cn.hgy.ai.cases.mcp.IMcpMessageService;
import cn.hgy.ai.cases.mcp.IMcpSessionService;
import cn.hgy.ai.domain.session.model.entity.HandleMessageCommandEntity;
import cn.hgy.ai.types.enums.ResponseCode;
import cn.hgy.ai.types.exception.AppException;
import com.alibaba.fastjson.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;


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

    private final IMcpSessionService mcpSessionService;
    private final IMcpMessageService mcpMessageService;

    /**
     * 建立 MCP SSE 长连接
     * 
     * 流程：
     * 1. 验证网关ID合法性
     * 2. 创建会话并初始化 SSE 流
     * 3. 返回 endpoint 事件，告知客户端后续消息发送地址
     * 
     * @param gatewayId 网关唯一标识
     * @param apiKey API密钥，用于后续请求认证
     * @return SSE 事件流，首个事件为 endpoint（包含 sessionId）
     * @throws Exception 网关不存在或其他系统异常
     */
    @GetMapping(value = "{gatewayId}/mcp/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Override
    public Flux<ServerSentEvent<String>> handleSSEConnection(@PathVariable("gatewayId") String gatewayId,
                                                             @RequestParam("apiKey") String apiKey) throws Exception {
        try {
            log.info("建立 MCP 连接 {}", gatewayId);

            if (StringUtils.isBlank(gatewayId)) {
                log.info("网关ID为空");
                throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
            }

            return mcpSessionService.createMcpSession(gatewayId, apiKey);
        } catch (AppException e) {
            log.error("建立 MCP 连接拒绝 {}", gatewayId, e);
            return Flux.just(ServerSentEvent.<String>builder()
                            .id(UUID.randomUUID().toString())
                            .event("error")
                            .data(JSON.toJSONString(Response.<String>builder()
                                    .code(e.getCode())
                                    .info(e.getInfo())
                                    .build()))
                    .build());
        } catch (Exception e) {
            log.error("建立 MCP 连接失败 {}", gatewayId);
            throw e;
        }
    }

    /**
     * 处理 MCP JSON-RPC 消息
     * 
     * 流程：
     * 1. 验证会话合法性（sessionId 必须存在且有效）
     * 2. 解析 JSON-RPC 请求（支持 initialize、tools/list、tools/call 等方法）
     * 3. 路由到对应的处理器执行业务逻辑
     * 4. 将响应通过 SSE 流推送回客户端
     * 
     * @param gatewayId 网关唯一标识
     * @param sessionId 会话ID（由 handleSSEConnection 返回）
     * @param apiKey API密钥，用于速率限制等认证
     * @param messageBody JSON-RPC 2.0 格式的请求消息
     * @return HTTP 202 Accepted（异步处理，响应通过 SSE 返回）
     */
    @PostMapping(value = "{gatewayId}/mcp/sse", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Void>> handleMessage(@PathVariable("gatewayId") String gatewayId,
                                                    @RequestParam("sessionId") String sessionId,
                                                    @RequestParam("apiKey") String apiKey,
                                                    @RequestBody String messageBody) {
        try {
            log.info("处理 MCP SSE 消息，gatewayId:{} sessionId:{} messageBody:{}", gatewayId, sessionId, messageBody);
            if (StringUtils.isBlank(gatewayId) || StringUtils.isBlank(sessionId)) {
                log.info("非法参数，gateway、sessionId is null");
                throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
            }

            HandleMessageCommandEntity commandEntity = new HandleMessageCommandEntity(gatewayId, apiKey, sessionId, messageBody);
            ResponseEntity<Void> responseEntity = mcpMessageService.handleMessage(commandEntity);

            return Mono.just(responseEntity);
        } catch (Exception e) {
            log.error("处理 MCP SSE 消息失败，gatewayId:{} sessionId:{} messageBody:{}", gatewayId, sessionId, messageBody, e);
            return Mono.just(ResponseEntity.internalServerError().build());
        }
    }
}
