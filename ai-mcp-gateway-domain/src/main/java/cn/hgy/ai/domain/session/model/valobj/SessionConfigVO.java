package cn.hgy.ai.domain.session.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Sinks;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-01-26
 * @Description: 会话配置值对象
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SessionConfigVO {

    /** 唯一标识 */
    private String sessionId;
    /** 流式响应 */
    private Sinks.Many<ServerSentEvent<String>> sink;
    /** 创建时间 */
    private Instant createTime;
    /** 最后一次访问时间 */
    private volatile Instant lastAccessTime;
    /** 活跃标识 */
    private volatile boolean active;

    public SessionConfigVO(String sessionId, Sinks.Many<ServerSentEvent<String>> sink) {
        this.sessionId = sessionId;
        this.sink = sink;
        this.createTime = Instant.now();
        this.lastAccessTime = Instant.now();
        this.active = true;
    }

    /** 标记会话为非活跃状态 */
    public void markInactive() {
        this.active = false;
    }

    /** 更新上次活跃时间 */
    public void updateLastAccessed() {
        this.lastAccessTime = Instant.now();
    }

    /** 判断是否过期  */
    public boolean isExpired(long timeoutMinutes) {
        return lastAccessTime.isBefore(Instant.now().minus(timeoutMinutes, ChronoUnit.MINUTES));
    }


}
