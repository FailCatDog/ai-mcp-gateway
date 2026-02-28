package cn.hgy.ai.domain.session.service.message;

import cn.hgy.ai.domain.session.model.valobj.McpSchemaVO;
import cn.hgy.ai.domain.session.model.valobj.enums.SessionMessageHandlerMethodEnum;
import cn.hgy.ai.domain.session.service.ISessionMessageService;
import cn.hgy.ai.domain.session.service.message.handler.IRequestHandler;
import cn.hgy.ai.types.enums.ResponseCode;
import cn.hgy.ai.types.exception.AppException;
import com.alibaba.fastjson.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-01-30
 * @Description:
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionMessageServiceImpl implements ISessionMessageService {

    private final Map<String, IRequestHandler> requestHandlerMap;

    @Override
    public McpSchemaVO.JSONRPCResponse processHandlerMessage(String gatewayId ,McpSchemaVO.JSONRPCMessage message) {

        if (message instanceof McpSchemaVO.JSONRPCResponse) {
            log.info("收到结果消息");
        }

        if (message instanceof McpSchemaVO.JSONRPCRequest request) {
            String method = request.method();
            log.info("开始处理请求 方法：{}", method);

            SessionMessageHandlerMethodEnum sessionMessageHandlerMethodEnum = SessionMessageHandlerMethodEnum.getByMethod(method);
            if (null == sessionMessageHandlerMethodEnum) {
                throw new AppException(ResponseCode.METHOD_NOT_FOUND.getCode(), ResponseCode.METHOD_NOT_FOUND.getInfo());
            }

            String handlerName = sessionMessageHandlerMethodEnum.getHandlerName();
            IRequestHandler requestHandler = requestHandlerMap.get(handlerName);
            if (null == requestHandler) {
                throw new AppException(ResponseCode.METHOD_NOT_FOUND.getCode(), ResponseCode.METHOD_NOT_FOUND.getInfo());
            }

            return requestHandler.handle(gatewayId, request);
        }

        if (message instanceof McpSchemaVO.JSONRPCNotification notification) {
            log.info("收到即将处理的消息通知 {} {}", notification.method(), JSON.toJSONString(notification.params()));
        }

        return null;
    }
}
