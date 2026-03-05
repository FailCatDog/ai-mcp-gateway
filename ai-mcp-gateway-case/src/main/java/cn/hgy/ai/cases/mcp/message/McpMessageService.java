package cn.hgy.ai.cases.mcp.message;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import cn.hgy.ai.cases.mcp.IMcpMessageService;
import cn.hgy.ai.cases.mcp.message.factory.DefaultMcpMessageFactory;
import cn.hgy.ai.domain.session.model.entity.HandleMessageCommandEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-03-05
 * @Description:
 */
@Service
@RequiredArgsConstructor
public class McpMessageService implements IMcpMessageService {

    private final DefaultMcpMessageFactory defaultMcpMessageFactory;

    @Override
    public ResponseEntity<Void> handleMessage(HandleMessageCommandEntity commandEntity) throws Exception {

        StrategyHandler<HandleMessageCommandEntity, DefaultMcpMessageFactory.DynamicContext, ResponseEntity<Void>> strategyHandler = defaultMcpMessageFactory.strategyHandler();

        return strategyHandler.apply(commandEntity, new DefaultMcpMessageFactory.DynamicContext());
    }
}
