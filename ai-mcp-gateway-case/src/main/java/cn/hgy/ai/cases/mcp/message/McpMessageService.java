package cn.hgy.ai.cases.mcp.message;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import cn.hgy.ai.cases.mcp.IMcpMessageService;
import cn.hgy.ai.cases.mcp.message.factory.DefaultMcpMessageFactory;
import cn.hgy.ai.domain.session.model.entity.HandleMessageCommandEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-03-05
 * @Description:
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class McpMessageService implements IMcpMessageService {

    private final DefaultMcpMessageFactory defaultMcpMessageFactory;

    /**
     * 处理 MCP 消息的业务编排
     * 
     * 采用策略树模式，按顺序执行：
     * 1. RootNode - 速率限制检查（tools/call 方法触发）
     * 2. SessionNode - 会话有效性验证
     * 3. MessageHandlerNode - 路由到具体的 JSON-RPC 方法处理器
     * 
     * @param commandEntity 包含 gatewayId、sessionId、apiKey、JSON-RPC 消息
     * @return HTTP 202 Accepted（响应通过 SSE 异步推送）
     * @throws Exception 会话不存在或处理异常
     */
    @Override
    public ResponseEntity<Void> handleMessage(HandleMessageCommandEntity commandEntity) throws Exception {
        StrategyHandler<HandleMessageCommandEntity, DefaultMcpMessageFactory.DynamicContext, ResponseEntity<Void>> strategyHandler = defaultMcpMessageFactory.strategyHandler();
        return strategyHandler.apply(commandEntity, new DefaultMcpMessageFactory.DynamicContext());
    }
}
