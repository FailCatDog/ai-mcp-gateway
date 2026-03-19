package cn.hgy.ai.domain.protocol.service;

import cn.hgy.ai.domain.protocol.model.entity.AnalysisCommandEntity;
import cn.hgy.ai.domain.protocol.model.valobj.http.HTTPProtocolVO;

import java.util.List;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-03-03
 * @Description: 协议解析领域服务接口
 */
public interface IProtocolAnalysis {

    List<HTTPProtocolVO> doAnalysis(AnalysisCommandEntity commandEntity);

}
