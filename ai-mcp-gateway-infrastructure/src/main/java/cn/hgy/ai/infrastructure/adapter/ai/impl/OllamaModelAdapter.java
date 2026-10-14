package cn.hgy.ai.infrastructure.adapter.ai;

import cn.hgy.ai.domain.session.model.valobj.gateway.McpToolProtocolConfigVO;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

@Component("ollama")
@RequiredArgsConstructor
@ConditionalOnBean(OllamaChatModel.class)
public class OllamaModelAdapter implements IAiModelAdapter {

    private final OllamaChatModel chatModel;

    @Override
    public String modelType() {
        return "ollama";
    }

    @Override
    public Object call(McpToolProtocolConfigVO.AIModelConfig config, Object params) {
        return ChatClient.create(chatModel)
                .prompt()
                .system(config.getPrompt())
                .user(params.toString())
                .call()
                .content();
    }
}

