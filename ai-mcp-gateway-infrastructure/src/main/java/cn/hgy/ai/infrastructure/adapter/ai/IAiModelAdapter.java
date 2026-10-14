package cn.hgy.ai.infrastructure.adapter.ai;

import cn.hgy.ai.domain.session.model.valobj.gateway.McpToolProtocolConfigVO;

public interface IAiModelAdapter {

    String modelType();

    Object call(McpToolProtocolConfigVO.AIModelConfig config, Object params);
}

