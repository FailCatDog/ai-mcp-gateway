package cn.hgy.ai.cases.mcp.message.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import cn.hgy.ai.cases.mcp.message.AbstractMcpMessageSupport;
import cn.hgy.ai.cases.mcp.message.factory.DefaultMcpMessageFactory;
import cn.hgy.ai.domain.auth.model.entity.RateLimitCommandEntity;
import cn.hgy.ai.domain.auth.service.IAuthRateLimitService;
import cn.hgy.ai.domain.session.model.entity.HandleMessageCommandEntity;
import cn.hgy.ai.domain.session.model.valobj.McpSchemaVO;
import cn.hgy.ai.domain.session.model.valobj.enums.SessionMessageHandlerMethodEnum;
import cn.hgy.ai.types.enums.McpErrorCodes;
import cn.hgy.ai.types.exception.AppException;
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
@Service("mcpMessageRootNode")
@RequiredArgsConstructor
public class RootNode  extends AbstractMcpMessageSupport {

    private final SessionNode sessionNode;

    private final IAuthRateLimitService authRateLimitService;

    @Override
    protected ResponseEntity<Void> doApply(HandleMessageCommandEntity requestParameter, DefaultMcpMessageFactory.DynamicContext dynamicContext) throws Exception {
        try {
            log.info("消息处理 mcp message RootNode: {}", requestParameter);
            if (requestParameter.getJsonrpcMessage() instanceof McpSchemaVO.JSONRPCRequest request) {
                String method = request.method();

                SessionMessageHandlerMethodEnum sessionMessageHandlerMethodEnum = SessionMessageHandlerMethodEnum.getByMethod(method);
                if (SessionMessageHandlerMethodEnum.TOOLS_CALL.equals(sessionMessageHandlerMethodEnum)) {
                    boolean isHit = authRateLimitService.rateLimit(new RateLimitCommandEntity(requestParameter.getGatewayId(), requestParameter.getApiKey()));
                    if (isHit) {
                        log.warn("消息处理 mcp message RootNode - 命中限流 {} {}", requestParameter.getGatewayId(), requestParameter.getApiKey());
                        throw new AppException(McpErrorCodes.INSUFFICIENT_PERMISSIONS, "fail to auth apiKey rateLimiter");
                    }
                }
            }

            return router(requestParameter, dynamicContext);
        }catch (Exception e) {
            log.info("消息处理 mcp message RootNode 异常: {}", requestParameter);
            throw e;
        }
    }

    @Override
    public StrategyHandler<HandleMessageCommandEntity, DefaultMcpMessageFactory.DynamicContext, ResponseEntity<Void>> get(HandleMessageCommandEntity handleMessageCommandEntity, DefaultMcpMessageFactory.DynamicContext dynamicContext) throws Exception {
        return sessionNode;
    }
}
