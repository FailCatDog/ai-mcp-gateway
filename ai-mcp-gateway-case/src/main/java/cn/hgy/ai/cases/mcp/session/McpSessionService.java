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

    /**
     * 创建 MCP 会话并返回 SSE 流
     * 
     * 采用策略树模式，按顺序执行：
     * 1. RootNode - 网关基本信息加载
     * 2. VerifyNode - 网关授权状态验证
     * 3. SessionNode - 会话创建与初始化
     * 
     * @param gatewayId 网关唯一标识
     * @param apiKey API密钥
     * @return SSE 事件流，首个事件为 endpoint（包含 sessionId 和后续请求地址）
     * @throws Exception 网关不存在或授权失败
     */
    @Override
    public Flux<ServerSentEvent<String>> createMcpSession(String gatewayId, String apiKey) throws Exception {
        StrategyHandler<String, DefaultMcpSessionFactory.DynamicContext, Flux<ServerSentEvent<String>>> strategyHandler = defaultMcpSessionFactory.strategyHandler();

        DefaultMcpSessionFactory.DynamicContext dynamicContext = new DefaultMcpSessionFactory.DynamicContext();
        dynamicContext.setApiKey(apiKey);

        return strategyHandler.apply(gatewayId, dynamicContext);
    }
}
