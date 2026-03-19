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

    @Override
    public SessionConfigVO createSession(String gatewayId, String apiKey) {

        String sessionId = UUID.randomUUID().toString();

        Sinks.Many<ServerSentEvent<String>> sink = Sinks.many().multicast().onBackpressureBuffer();


        // 发送端点消息 - 告知客户第二次请求地址
        String messageEndpoint = "/api-gateway/" + gatewayId +"/mcp/sse?sessionId=" + sessionId;
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
