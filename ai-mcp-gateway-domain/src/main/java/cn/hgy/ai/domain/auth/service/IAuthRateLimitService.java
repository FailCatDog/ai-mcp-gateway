package cn.hgy.ai.domain.auth.service;

import cn.hgy.ai.domain.auth.model.entity.RateLimitCommandEntity;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-03-05
 * @Description:
 */
public interface IAuthRateLimitService {
    /**
     * true 限流
     * false 未命中
     * @param commandEntity
     * @return
     */
    boolean rateLimit(RateLimitCommandEntity commandEntity);
}
