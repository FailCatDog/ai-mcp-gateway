package cn.hgy.ai.domain.protocol.service.analysis.strategy;

import cn.hgy.ai.domain.protocol.model.valobj.http.HTTPProtocolVO;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-03-03
 * @Description: 协议解析策略抽象基类，提供属性遍历与类型转换公共能力
 */
public abstract class AbstractProtocolAnalysisStrategy implements IProtocolAnalysisStrategy {

    /**
     * 递归解析 OpenAPI Schema 的 properties，构建平铺的 ProtocolMapping 列表
     *
     * 将 OpenAPI 嵌套 Schema（支持 $ref 引用）还原为带 parentPath 的平铺映射结构，
     * 用于后续在 tools/list 中重建 JSON Schema 树，以及在 tools/call 中进行参数映射。
     *
     * 示例：Schema { city: string, company: { name: string } }
     * 还原为：
     *   { parentPath: rootName,          fieldName: city,    mcpPath: rootName.city }
     *   { parentPath: rootName,          fieldName: company, mcpPath: rootName.company }
     *   { parentPath: rootName.company,  fieldName: name,    mcpPath: rootName.company.name }
     *
     * @param parentMcpPath 父节点的 MCP 路径（用于构建 a.b.c 关系链）
     * @param properties    当前 Schema 的 properties 节点
     * @param requiredList  当前 Schema 的 required 数组（用于标记必填字段）
     * @param definitions   components/schemas 全局定义，用于解析 $ref 引用
     * @param mappings      结果收集列表
     */
    protected void parseProperties(String parentMcpPath, JSONObject properties, JSONArray requiredList, JSONObject definitions, List<HTTPProtocolVO.ProtocolMapping> mappings) {
        if (properties == null) return;

        int sortOrder = 1;

        for (String propName : properties.keySet()) {
            JSONObject prop = properties.getJSONObject(propName);

            // 拼接关系链 a.b.c
            String currentMcpPath = parentMcpPath + "." + propName;

            JSONObject effectiveSchema = prop;
            String type = prop.getString("type");
            String description = prop.getString("description");

            // 获取应用关系对象
            if (prop.containsKey("$ref")) {
                String ref = prop.getString("$ref");
                String refName = ref.substring(ref.lastIndexOf('/') + 1);
                effectiveSchema = definitions.getJSONObject(refName);
                if (type == null) type = effectiveSchema.getString("type");
                if (description == null) description = effectiveSchema.getString("description");
            }

            HTTPProtocolVO.ProtocolMapping mapping = HTTPProtocolVO.ProtocolMapping.builder()
                    .mappingType("request")
                    .parentPath(parentMcpPath)
                    .fieldName(propName)
                    .mcpPath(currentMcpPath)
                    .mcpType(convertType(type))
                    .mcpDesc(description)
                    .isRequired(requiredList != null && requiredList.contains(propName) ? 1 : 0)
                    .sortOrder(sortOrder++)
                    .build();
            mappings.add(mapping);

            // 如果存在下一个引用对象，则嵌套循环继续寻找
            if (effectiveSchema.containsKey("properties")) {
                parseProperties(currentMcpPath, effectiveSchema.getJSONObject("properties"), effectiveSchema.getJSONArray("required"), definitions, mappings);
            }
        }
    }

    protected String convertType(String type) {
        if (type == null) return "string";
        return switch (type.toLowerCase()) {
            case "string", "char", "date", "datetime" -> "string";
            case "integer", "int", "long", "double", "float", "number" -> "number";
            case "boolean", "bool" -> "boolean";
            case "array", "list" -> "array";
            default -> "object";
        };
    }

    protected String toLowerCamel(String name) {
        if (name == null || name.isEmpty()) return name;
        char[] cs = name.toCharArray();
        cs[0] = Character.toLowerCase(cs[0]);
        return new String(cs);
    }

}
