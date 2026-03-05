package cn.hgy.ai.domain.auth.service;

import cn.hgy.ai.domain.auth.model.entity.LicenseCommandEntity;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-03-05
 * @Description: 权限证书服务接口
 */
public interface IAuthLicenseService {
    boolean checkLicense(LicenseCommandEntity commandEntity);
}
