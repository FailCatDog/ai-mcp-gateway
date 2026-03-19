package cn.hgy.ai.cases.mcp;

import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-01-29
 * @Description:
 */
public interface IMcpSessionService {

    Flux<ServerSentEvent<String>> createMcpSession(String gatewayId, String apiKey) throws Exception;
}
