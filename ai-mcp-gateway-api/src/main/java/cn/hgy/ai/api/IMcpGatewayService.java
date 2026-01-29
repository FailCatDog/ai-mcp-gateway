package cn.hgy.ai.api;

import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-01-29
 * @Description:
 */
public interface IMcpGatewayService {

    Flux<ServerSentEvent<String>> establishSSEConnection(String gatewayId) throws Exception;
}
