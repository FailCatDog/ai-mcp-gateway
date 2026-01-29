package cn.hgy.ai.cases.mcp.session;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import cn.hgy.ai.cases.mcp.IMcpSessionService;
import cn.hgy.ai.cases.mcp.session.factory.DefaultMcpSessionFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-01-29
 * @Description:
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class McpSessionService implements IMcpSessionService {

    private final DefaultMcpSessionFactory defaultMcpSessionFactory;

    @Override
    public Flux<ServerSentEvent<String>> createMcpSession(String gatewayId) throws Exception {
        StrategyHandler<String, DefaultMcpSessionFactory.DynamicContext, Flux<ServerSentEvent<String>>> strategyHandler = defaultMcpSessionFactory.strategyHandler();

        return strategyHandler.apply(gatewayId, new DefaultMcpSessionFactory.DynamicContext());
    }
}
