package cn.hgy.ai.domain.session.model.valobj;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-01-29
 * @Description:
 */
public class McpSchemaVO {

    /** TypeReference是 Jackson 框架提供的泛型类型保留工具类
     * 给 Jackson 框架明确指定「要反序列化的目标类型是HashMap<String, Object>」，
     * 让 Jackson 能精准解析出键为String、值为Object的 HashMap，而非无泛型的原始 HashMap。 */
    private static final TypeReference<HashMap<String, Object>> MAP_TYPE_REF = new TypeReference<>() {};

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static JSONRPCMessage deserializeJsonMessage(String jsonText) throws JsonProcessingException {

        // 1. 将JSON字符串解析成HashMap，用于判断消息类型
        HashMap<String, Object> map = objectMapper.readValue(jsonText, MAP_TYPE_REF);

        // 2. 根据协议特征判断消息是请求还是响应
        if (map.containsKey("method") && map.containsKey("id")) {
            return objectMapper.convertValue(map, JSONRPCRequest.class);
        }else if (map.containsKey("method") && !map.containsKey("id")) {
            return objectMapper.convertValue(map, JSONRPCNotification.class);
        } else if (map.containsKey("result") && map.containsKey("error")) {
            return objectMapper.convertValue(map, JSONRPCResponse.class);
        }

        // 3. 不是标准协议抛出非法参数异常
        throw new IllegalArgumentException("error: " + jsonText);
    }

    /** permits 关键字用于密封接口，只允许指定的类进行实现 */
    public sealed interface JSONRPCMessage permits JSONRPCRequest, JSONRPCResponse, JSONRPCNotification {
        // 所有rpc协议必须包含jsonrpc字段
        String jsonrpc();
    }

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record JSONRPCRequest(
            @JsonProperty("jsonrpc") String jsonrpc,
            @JsonProperty("method") String method,
            @JsonProperty("id") Object id,
            @JsonProperty("params") Object params
    )implements JSONRPCMessage{
    }

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record JSONRPCNotification (
            @JsonProperty("jsonrpc") String jsonrpc,
            @JsonProperty("method") String method,
            @JsonProperty("params") Object param
    ) implements JSONRPCMessage {
    }

    /** record记录类 只需要指定字段列表，无前缀的getter方法，适用于Java类和JSON字段的转化 */
    @JsonInclude(JsonInclude.Include.NON_ABSENT)    // 序列化的时候排除空值字段，用于减少JSON数据体积
    @JsonIgnoreProperties(ignoreUnknown = true)     // 反序列化的时候忽略JSON中存在但是Java类中无对应字段的键
    public record JSONRPCResponse(
            @JsonProperty("jsonrpc") String jsonrpc,
            @JsonProperty("id") Object id,
            @JsonProperty("result") Object result,
            @JsonProperty("error") JSONRPCError error
    )implements JSONRPCMessage{
        @JsonInclude(JsonInclude.Include.NON_ABSENT)
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record JSONRPCError(
                @JsonProperty("code") int code,
                @JsonProperty("message") String message,
                @JsonProperty("data") Object data
        ){
        }
    }
}
