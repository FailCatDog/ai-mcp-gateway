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

    private static final TypeReference<HashMap<String, Object>> MAP_TYPE_REF = new TypeReference<>() {};

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static JSONRPCMessage deserializeJsonMessage(String jsonText) throws JsonProcessingException {
        HashMap<String, Object> map = objectMapper.readValue(jsonText, MAP_TYPE_REF);

        if (map.containsKey("method") && map.containsKey("id")) {
            return objectMapper.convertValue(map, JSONRPCRequest.class);
        } else if (map.containsKey("result") && map.containsKey("error")) {
            return objectMapper.convertValue(map, JSONRPCResponse.class);
        }

        throw new IllegalArgumentException("error: " + jsonText);
    }

    public sealed interface JSONRPCMessage permits JSONRPCRequest, JSONRPCResponse {
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
