package cn.hgy.ai.infrastructure.adapter.ai;

import cn.hgy.ai.domain.session.model.valobj.gateway.McpToolProtocolConfigVO;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AiModelAdapterFactory {

    private final Map<String, IAiModelAdapter> adapterMap;

    public AiModelAdapterFactory(Map<String, IAiModelAdapter> adapterMap) {
        this.adapterMap = adapterMap;
    }

    public Object call(McpToolProtocolConfigVO.AIModelConfig config, Object params) {
        IAiModelAdapter adapter = adapterMap.get(config.getModelType());
        if (adapter == null) {
            throw new IllegalArgumentException("不支持的模型类型: " + config.getModelType());
        }
        return adapter.call(config, params);
    }
}

