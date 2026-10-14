package cn.hgy.ai.cases.mcp.message.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import cn.hgy.ai.cases.mcp.message.AbstractMcpMessageSupport;
import cn.hgy.ai.cases.mcp.message.factory.DefaultMcpMessageFactory;
import cn.hgy.ai.domain.session.model.entity.HandleMessageCommandEntity;
import cn.hgy.ai.domain.session.model.valobj.SessionConfigVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-03-05
 * @Description:
 */
@Slf4j
@Service("mcpMessageSessionNode")
@RequiredArgsConstructor
public class SessionNode extends AbstractMcpMessageSupport {

    private final MessageHandlerNode messageHandlerNode;


    @Override
    protected ResponseEntity<Void> doApply(HandleMessageCommandEntity requestParameter, DefaultMcpMessageFactory.DynamicContext dynamicContext) throws Exception {
        log.info("消息处理 mcp message SessionNode:{}", requestParameter);

        SessionConfigVO sessionConfigVO = sessionManagementService.getSession(requestParameter.getSessionId());
        if (null == sessionConfigVO) {
            log.warn("会话信息不存在 {}", requestParameter);
            return ResponseEntity.notFound().build();
        }

        dynamicContext.setSessionConfigVO(sessionConfigVO);

        return router(requestParameter, dynamicContext);
    }

    @Override
    public StrategyHandler<HandleMessageCommandEntity, DefaultMcpMessageFactory.DynamicContext, ResponseEntity<Void>> get(HandleMessageCommandEntity handleMessageCommandEntity, DefaultMcpMessageFactory.DynamicContext dynamicContext) throws Exception {
        return messageHandlerNode;
    }
}
