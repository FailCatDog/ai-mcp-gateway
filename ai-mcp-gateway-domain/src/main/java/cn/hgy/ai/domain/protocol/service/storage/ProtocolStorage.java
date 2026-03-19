package cn.hgy.ai.domain.protocol.service.storage;

import cn.hgy.ai.domain.protocol.adapter.repository.IProtocolRepository;
import cn.hgy.ai.domain.protocol.model.entity.StorageCommandEntity;
import cn.hgy.ai.domain.protocol.service.IProtocolStorage;
import jakarta.annotation.Resource;

import java.util.List;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-03-03
 * @Description: 协议存储服务实现
 */
public class ProtocolStorage implements IProtocolStorage {

    @Resource
    private IProtocolRepository repository;

    @Override
    public List<Long> doStorage(StorageCommandEntity commandEntity) {
        return repository.saveHttpProtocolAndMapping(commandEntity.getHttpProtocolVOS());
    }

}
