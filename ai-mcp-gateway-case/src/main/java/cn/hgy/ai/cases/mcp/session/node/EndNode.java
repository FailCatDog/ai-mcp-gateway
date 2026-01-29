package cn.hgy.ai.cases.mcp.session.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import cn.hgy.ai.cases.mcp.session.AbstractMcpSessionSupport;
import cn.hgy.ai.cases.mcp.session.factory.DefaultMcpSessionFactory;
import cn.hgy.ai.domain.session.model.valobj.SessionConfigVO;
import cn.hgy.ai.domain.session.service.ISessionManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-01-29
 * @Description:
 */
@Slf4j
@Service
public class EndNode extends AbstractMcpSessionSupport {

    public EndNode(ISessionManagementService sessionManagementService) {
        super(sessionManagementService);
    }

    @Override
    protected Flux<ServerSentEvent<String>> doApply(String string, DefaultMcpSessionFactory.DynamicContext dynamicContext) throws Exception {

        SessionConfigVO sessionConfigVO = dynamicContext.getSessionConfigVO();
        String sessionId = sessionConfigVO.getSessionId();

        Sinks.Many<ServerSentEvent<String>> sink = sessionConfigVO.getSink();

        return sink.asFlux()
                // 心跳机制
                .mergeWith(
                        Flux.interval(Duration.ofSeconds(60))
                                .map(i -> ServerSentEvent.<String>builder()
                                        .event("ping")
                                        .data("ping")
                                        .build())
                )
                .doOnCancel(() -> {
                        sessionManagementService.removeSession(sessionId);
                })
                .doOnTerminate(() -> {
                    sessionManagementService.removeSession(sessionId);
                });
    }

    @Override
    public StrategyHandler<String, DefaultMcpSessionFactory.DynamicContext, Flux<ServerSentEvent<String>>> get(String string, DefaultMcpSessionFactory.DynamicContext dynamicContext) throws Exception {
        return null;
    }
}
