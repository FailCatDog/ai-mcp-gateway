package cn.hgy.ai.infrastructure.adapter.ai;

import cn.hgy.ai.domain.session.model.valobj.gateway.McpToolProtocolConfigVO;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

@Component("openai")
@RequiredArgsConstructor
@ConditionalOnBean(OpenAiChatModel.class)
public class OpenAiModelAdapter implements IAiModelAdapter {

    private final OpenAiChatModel chatModel;

    @Override
    public String modelType() {
        return "openai";
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

