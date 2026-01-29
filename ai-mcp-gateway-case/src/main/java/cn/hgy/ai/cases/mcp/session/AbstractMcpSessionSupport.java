package cn.hgy.ai.cases.mcp.session;

import cn.bugstack.wrench.design.framework.tree.AbstractMultiThreadStrategyRouter;
import cn.hgy.ai.cases.mcp.session.factory.DefaultMcpSessionFactory;
import cn.hgy.ai.domain.session.service.ISessionManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

import java.util.concurrent.ExecutionException;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-01-29
 * @Description:
 */
@RequiredArgsConstructor
public abstract class AbstractMcpSessionSupport extends AbstractMultiThreadStrategyRouter<String, DefaultMcpSessionFactory.DynamicContext, Flux<ServerSentEvent<String>>> {

    protected final ISessionManagementService sessionManagementService;

    @Override
    protected void multiThread(String requestParameter, DefaultMcpSessionFactory.DynamicContext dynamicContext) throws ExecutionException, InterruptedException {

    }
}
