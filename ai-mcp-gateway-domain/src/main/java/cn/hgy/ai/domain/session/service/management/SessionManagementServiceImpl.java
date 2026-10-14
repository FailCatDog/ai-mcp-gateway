package cn.hgy.ai.domain.session.service.management;

import cn.hgy.ai.domain.session.model.valobj.SessionConfigVO;
import cn.hgy.ai.domain.session.service.ISessionManagementService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-01-26
 * @Description:
 */
@Service
@Slf4j
public class SessionManagementServiceImpl implements ISessionManagementService {

    private static final long SESSION_TIMEOUT_MINUTES = 30L;

    /** ScheduledExecutorService 用于解决 延迟执行任务 和 周期性执行任务
     * Executors 是线程工具池类 newSingleThreadScheduledExecutor 用于创建只有1个工作线程的实例 所有提交的定时任务都会在这个线程里串行执行 */
    private final ScheduledExecutorService cleanupScheduler = Executors.newSingleThreadScheduledExecutor();

    private final Map<String, SessionConfigVO> activeSessions = new ConcurrentHashMap<>();

    public SessionManagementServiceImpl() {
        cleanupScheduler.scheduleAtFixedRate(this::cleanupExpiredSessions, 5, 5, TimeUnit.MINUTES);
    }

    /**
     * 创建新的 MCP 会话
     * 
     * 1. 生成唯一的 sessionId
     * 2. 创建 Reactor Sink 用于异步推送 SSE 事件
     * 3. 发送 endpoint 事件，告知客户端后续消息发送地址
     * 4. 将会话存储到内存，启动自动过期清理机制
     * 
     * @param gatewayId 网关ID
     * @param apiKey API密钥
     * @return 会话配置对象，包含 sessionId 和 SSE 事件流
     */
    @Override
    public SessionConfigVO createSession(String gatewayId, String apiKey) {
        String sessionId = UUID.randomUUID().toString();
        Sinks.Many<ServerSentEvent<String>> sink = Sinks.many().multicast().onBackpressureBuffer();

        String messageEndpoint = "/api-gateway/" + gatewayId + "/mcp/sse?sessionId=" + sessionId;
        if (StringUtils.isNotBlank(apiKey)) {
            messageEndpoint += "&apiKey=" + apiKey;
        }
        sink.tryEmitNext(ServerSentEvent.<String>builder()
                .event("endpoint")
                .data(messageEndpoint)
                .build());

        SessionConfigVO sessionConfigVO = new SessionConfigVO(sessionId, sink);
        activeSessions.put(sessionId, sessionConfigVO);

        log.info("创建会话 gatewayId:{}", gatewayId);
        return sessionConfigVO;
    }

    @Override
    public void removeSession(String sessionId) {
        SessionConfigVO sessionConfigVO = new SessionConfigVO();
        if (null == sessionId) return;

        sessionConfigVO.markInactive();

        try {
            sessionConfigVO.getSink().tryEmitComplete();
        }catch (Exception e) {
            log.warn("关闭会话失败", e);
        }
    }

    /**
     * 获取活跃会话
     * 
     * 1. 检查会话是否存在且处于活跃状态
     * 2. 更新最后访问时间（用于过期判定）
     * 3. 返回会话对象供消息处理使用
     * 
     * @param sessionId 会话ID
     * @return 活跃会话对象，若不存在或已过期则返回 null
     */
    @Override
    public SessionConfigVO getSession(String sessionId) {
        if (null == sessionId || sessionId.isEmpty()) return null;

        SessionConfigVO sessionConfigVO = activeSessions.get(sessionId);

        if (null != sessionConfigVO && sessionConfigVO.isActive()) {
            sessionConfigVO.updateLastAccessed();
            return sessionConfigVO;
        }

        return null;
    }

    /**
     * 定期清理过期会话
     * 
     * 由 ScheduledExecutorService 每 5 分钟执行一次
     * 清理条件：会话已标记为非活跃 或 超过 30 分钟未访问
     * 
     * @see #SESSION_TIMEOUT_MINUTES 会话超时时间（分钟）
     */
    @Override
    public void cleanupExpiredSessions() {
        int cleanedCount = 0;

        for (Map.Entry<String, SessionConfigVO> entry : activeSessions.entrySet()) {
            SessionConfigVO sessionConfigVO = entry.getValue();

            if (!sessionConfigVO.isActive() || sessionConfigVO.isExpired(SESSION_TIMEOUT_MINUTES)) {
                removeSession(sessionConfigVO.getSessionId());
                cleanedCount++;
            }
        }

        if (cleanedCount > 0) {
            log.info("清理了{}", cleanedCount);
        }
    }

    @Override
    public void shutdown() {
        for (String sessionId : activeSessions.keySet()) {
            removeSession(sessionId);
        }

        cleanupScheduler.shutdown();

        try {
            if (!cleanupScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                cleanupScheduler.shutdown();
            }
        }catch (InterruptedException e) {
            cleanupScheduler.shutdown();
            Thread.currentThread().interrupt();
        }
    }
}
