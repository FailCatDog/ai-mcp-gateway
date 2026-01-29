package cn.hgy.ai.cases.mcp.session.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import cn.hgy.ai.cases.mcp.session.AbstractMcpSessionSupport;
import cn.hgy.ai.cases.mcp.session.factory.DefaultMcpSessionFactory;
import cn.hgy.ai.domain.session.model.valobj.SessionConfigVO;
import cn.hgy.ai.domain.session.service.ISessionManagementService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-01-29
 * @Description:
 */
@Slf4j
@Service
public class SessionNode extends AbstractMcpSessionSupport {

    public SessionNode(ISessionManagementService sessionManagementService) {
        super(sessionManagementService);
    }

    @Resource
    private EndNode endNode;

    @Override
    protected Flux<ServerSentEvent<String>> doApply(String requestParameter, DefaultMcpSessionFactory.DynamicContext dynamicContext) throws Exception {

        SessionConfigVO sessionConfigVO = sessionManagementService.createSession(requestParameter);
        dynamicContext.setSessionConfigVO(sessionConfigVO);

        return router(requestParameter, dynamicContext);
    }

    @Override
    public StrategyHandler<String, DefaultMcpSessionFactory.DynamicContext, Flux<ServerSentEvent<String>>> get(String string, DefaultMcpSessionFactory.DynamicContext dynamicContext) throws Exception {
        return endNode;
    }
}
