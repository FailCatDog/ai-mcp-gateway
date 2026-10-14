package cn.hgy.ai.domain.protocol.service.analysis.strategy.impl;

import cn.hgy.ai.domain.protocol.model.valobj.http.HTTPProtocolVO;
import cn.hgy.ai.domain.protocol.service.analysis.strategy.AbstractProtocolAnalysisStrategy;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-03-03
 * @Description: requestBody 请求体协议解析策略
 */
@Slf4j
@Component("requestBodyAnalysis")
@Order(1)
public class RequestBodyAnalysisStrategy extends AbstractProtocolAnalysisStrategy {

    @Override
    public void doAnalysis(JSONObject operation, JSONObject definitions, List<HTTPProtocolVO.ProtocolMapping> mappings) {
        // 1. 提取 requestBody -> content -> application/json -> schema
        JSONObject requestBody = operation.getJSONObject("requestBody");
        if (requestBody == null) return;

        JSONObject content = requestBody.getJSONObject("content");
        JSONObject appJson = content.getJSONObject("application/json");
        if (appJson == null) return;

        JSONObject schema = appJson.getJSONObject("schema");
        String ref = schema.getString("$ref");

        if (ref != null) {
            // 2. 解析 $ref 引用，从 components/schemas 中找到对应的 Schema 定义
            String refName = ref.substring(ref.lastIndexOf('/') + 1);
            JSONObject reqSchema = definitions.getJSONObject(refName);

            // 3. 将 Schema 名称转为 lowerCamelCase 作为 MCP 根节点名（如 CreateOrderRequest -> createOrderRequest）
            String rootName = toLowerCamel(refName);

            // 4. 构建根节点 ProtocolMapping（parentPath 为 null 表示顶层属性）
            HTTPProtocolVO.ProtocolMapping rootMapping = HTTPProtocolVO.ProtocolMapping.builder()
                    .mappingType("request")
                    .parentPath(null)
                    .fieldName(rootName)
                    .mcpPath(rootName)
                    .mcpType(convertType(reqSchema.getString("type")))
                    .mcpDesc(reqSchema.getString("description"))
                    .isRequired(1)
                    .sortOrder(1)
                    .build();

            mappings.add(rootMapping);

            // 5. 递归解析 Schema 的 properties，构建完整的嵌套 ProtocolMapping 列表
            parseProperties(rootName, reqSchema.getJSONObject("properties"), reqSchema.getJSONArray("required"), definitions, mappings);
        }
    }

}
