package cn.hgy.ai.domain.auth.service;

import cn.hgy.ai.domain.auth.model.entity.RegisterCommandEntity;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-03-05
 * @Description:
 */
public interface IAuthRegisterService {

    String register(RegisterCommandEntity commandEntity);
}
