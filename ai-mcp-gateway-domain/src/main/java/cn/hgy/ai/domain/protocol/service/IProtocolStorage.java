package cn.hgy.ai.domain.protocol.service;

import cn.hgy.ai.domain.protocol.model.entity.StorageCommandEntity;

import java.util.List;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-03-03
 * @Description: 协议存储领域服务接口
 */
public interface IProtocolStorage {
    List<Long> doStorage(StorageCommandEntity commandEntity);
}
