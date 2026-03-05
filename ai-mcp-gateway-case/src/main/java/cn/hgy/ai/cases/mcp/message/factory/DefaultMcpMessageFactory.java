package cn.hgy.ai.cases.mcp.message.factory;

import cn.bugstack.wrench.design.framework.tree.DynamicContext;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import cn.hgy.ai.cases.mcp.message.node.RootNode;
import cn.hgy.ai.cases.mcp.session.factory.DefaultMcpSessionFactory;
import cn.hgy.ai.domain.session.model.entity.HandleMessageCommandEntity;
import cn.hgy.ai.domain.session.model.valobj.SessionConfigVO;
import jakarta.annotation.Resource;
import lombok.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
/**
 * @Author: 黄光宇
 * @CreateTime: 2026-03-05
 * @Description:
 */
@Service
@RequiredArgsConstructor
public class DefaultMcpMessageFactory {

    @Qualifier("mcpMessageRootNode")
    private final RootNode rootNode;

    public StrategyHandler<HandleMessageCommandEntity, DefaultMcpMessageFactory.DynamicContext, ResponseEntity<Void>> strategyHandler() {
        return rootNode;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DynamicContext extends cn.bugstack.wrench.design.framework.tree.DynamicContext {
        private SessionConfigVO sessionConfigVO;
    }
}
