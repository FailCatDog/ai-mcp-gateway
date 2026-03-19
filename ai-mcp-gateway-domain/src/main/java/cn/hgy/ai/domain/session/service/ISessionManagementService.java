package cn.hgy.ai.domain.session.service;

import cn.hgy.ai.domain.session.model.valobj.SessionConfigVO;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-01-26
 * @Description: 会话管理服务接口
 */
public interface ISessionManagementService {
    SessionConfigVO createSession(String getawayId, String apiKey);

    void removeSession(String sessionId);

    SessionConfigVO getSession(String sessionId);

    void cleanupExpiredSessions();

    void shutdown();
}
