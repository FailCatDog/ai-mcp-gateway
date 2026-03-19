package cn.hgy.ai.domain.protocol.service.analysis.strategy;

import cn.hgy.ai.domain.protocol.model.valobj.http.HTTPProtocolVO;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-03-03
 * @Description: 协议解析策略接口
 */
public interface IProtocolAnalysisStrategy {

    void doAnalysis(JSONObject operation, JSONObject definitions, List<HTTPProtocolVO.ProtocolMapping> mappings);

}
