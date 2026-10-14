package cn.hgy.ai.domain.session.service.message.handler.impl;

import cn.hgy.ai.domain.session.adapter.repository.ISessionRepository;
import cn.hgy.ai.domain.session.model.valobj.McpSchemaVO;
import cn.hgy.ai.domain.session.model.valobj.gateway.McpGatewayConfigVO;
import cn.hgy.ai.domain.session.model.valobj.gateway.McpToolConfigVO;
import cn.hgy.ai.domain.session.model.valobj.gateway.McpToolProtocolConfigVO;
import cn.hgy.ai.domain.session.service.message.handler.IRequestHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-01-30
 * @Description: 返回服务器支持的工具列表
 */
@Slf4j
@Service("toolsListHandler")
@RequiredArgsConstructor
public class ToolsListHandler implements IRequestHandler {

    private final ISessionRepository repository;

    /**
     * {
     * "tools": [
     * {
     * "description": "获取公司雇员信息",
     * "inputSchema": {
     * "additionalProperties": false,
     * "properties": {
     * "xxxRequest01": {
     * "type": "object",
     * "properties": {
     * "city": {
     * "type": "string",
     * "description": "城市名称,如果是中文汉字请先转换为汉语拼音,例如北京:beijing"
     * },
     * "company": {
     * "type": "object",
     * "properties": {
     * "name": {
     * "type": "string",
     * "description": "公司名称"
     * },
     * "type": {
     * "type": "string",
     * "description": "公司类型"
     * }
     * },
     * "required": [
     * "name",
     * "type"
     * ],
     * "description": "公司信息,如果是中文汉字请先转换为汉语拼音,例如北京:jd/alibaba"
     * }
     * },
     * "required": [
     * "city",
     * "company"
     * ]
     * },
     * "xxxRequest02": {
     * "type": "object",
     * "properties": {
     * "employeeCount": {
     * "type": "string",
     * "description": "雇员姓名"
     * }
     * },
     * "required": [
     * "employeeCount"
     * ]
     * }
     * },
     * "required": [
     * "xxxRequest01",
     * "xxxRequest02"
     * ],
     * "type": "object"
     * },
     * "name": "getCompanyEmployee"
     * }
     * ]
     * }
     */
    @Override
    public McpSchemaVO.JSONRPCResponse handle(String gatewayId, McpSchemaVO.JSONRPCRequest message) {

        // 1. 查询网关下注册的所有工具配置（工具名称、描述、协议映射关系等）
        List<McpToolConfigVO> mcpToolConfigVOS = repository.queryMcpGatewayToolConfigListByGatewayId(gatewayId);

        // 2. 将工具配置转换为 MCP 标准 Tool Schema 列表（含 inputSchema 的 JSON Schema 结构）
        List<McpSchemaVO.Tool> tools = buildTools(mcpToolConfigVOS);

        return new McpSchemaVO.JSONRPCResponse("2.0", message.id(), Map.of(
                "tools", tools
        ), null);
    }

    /**
     * 将工具配置列表转换为 MCP Tool Schema 列表
     * 
     * 核心逻辑：将数据库中平铺的协议映射记录（parent_path + field_name），
     * 还原为嵌套的 JSON Schema 树状结构（inputSchema），供 AI 客户端解析调用参数。
     * 
     * 示例还原结果：
     * xxxRequest01.city -> { type: string, description: "..." }
     * xxxRequest01.company.name -> { type: string, description: "..." }
     */
    private List<McpSchemaVO.Tool> buildTools(List<McpToolConfigVO> toolConfigs) {
        List<McpSchemaVO.Tool> tools = new ArrayList<>();

        for (McpToolConfigVO toolConfigVO : toolConfigs) {
            McpToolProtocolConfigVO mcpToolProtocolConfigVO = toolConfigVO.getMcpToolProtocolConfigVO();
            List<McpToolProtocolConfigVO.ProtocolMapping> configs = mcpToolProtocolConfigVO.getRequestProtocolMappings();

            // 按 sort_order 排序，保证 Schema 字段顺序与配置一致
            configs.sort((o1, o2) -> {
                int s1 = o1.getSortOrder() != null ? o1.getSortOrder() : 0;
                int s2 = o2.getSortOrder() != null ? o2.getSortOrder() : 0;
                return Integer.compare(s1, s2);
            });

            // 构建父子关系索引：parentPath -> List<子节点>
            // parentPath 为 null 的节点为根节点（对应 inputSchema 的顶层属性）
            Map<String, List<McpToolProtocolConfigVO.ProtocolMapping>> childrenMap = new HashMap<>();
            List<McpToolProtocolConfigVO.ProtocolMapping> roots = new ArrayList<>();

            for (McpToolProtocolConfigVO.ProtocolMapping config : configs) {
                if (config.getParentPath() == null) {
                    roots.add(config);
                } else {
                    childrenMap.computeIfAbsent(config.getParentPath(), k -> new ArrayList<>()).add(config);
                }
            }

            // 根节点按 sort_order 排序
            roots.sort((o1, o2) -> {
                int s1 = o1.getSortOrder() != null ? o1.getSortOrder() : 0;
                int s2 = o2.getSortOrder() != null ? o2.getSortOrder() : 0;
                return Integer.compare(s1, s2);
            });

            // 递归构建顶层 properties 和 required 数组
            Map<String, Object> properties = new HashMap<>();
            List<String> required = new ArrayList<>();

            for (McpToolProtocolConfigVO.ProtocolMapping root : roots) {
                properties.put(root.getFieldName(), buildProperty(root, childrenMap));
                if (Integer.valueOf(1).equals(root.getIsRequired())) {
                    required.add(root.getFieldName());
                }
            }

            // 单根节点时直接取其类型，多根节点统一为 object
            String type = roots.size() == 1 ? roots.get(0).getMcpType() : "object";

            // 组装 JSON Schema：type + properties + required + additionalProperties
            McpSchemaVO.JsonSchema inputSchema = new McpSchemaVO.JsonSchema(
                    type,
                    properties,
                    required.isEmpty() ? null : required,
                    false,
                    null,
                    null
            );

            tools.add(new McpSchemaVO.Tool(toolConfigVO.getToolName(), toolConfigVO.getToolDescription(), inputSchema));
        }

        return tools;
    }

    /**
     * 递归构建单个属性的 JSON Schema 节点
     * 
     * 根据当前节点的 mcpPath 在 childrenMap 中查找子节点，
     * 若存在子节点则递归构建嵌套的 properties 和 required，
     * 最终还原为完整的嵌套 JSON Schema 结构。
     * 
     * @param current    当前协议映射节点
     * @param childrenMap 父子关系索引（parentPath -> List<子节点>）
     * @return 当前节点对应的 JSON Schema Map
     */
    private Map<String, Object> buildProperty(McpToolProtocolConfigVO.ProtocolMapping current, Map<String, List<McpToolProtocolConfigVO.ProtocolMapping>> childrenMap) {
        Map<String, Object> property = new HashMap<>();
        property.put("type", current.getMcpType());
        if (current.getMcpDesc() != null) {
            property.put("description", current.getMcpDesc());
        }

        // 当前节点存在子节点，递归构建嵌套 properties 和 required
        List<McpToolProtocolConfigVO.ProtocolMapping> children = childrenMap.get(current.getMcpPath());
        if (children != null && !children.isEmpty()) {
            Map<String, Object> props = new HashMap<>();
            List<String> reqs = new ArrayList<>();

            children.sort((o1, o2) -> {
                int s1 = o1.getSortOrder() != null ? o1.getSortOrder() : 0;
                int s2 = o2.getSortOrder() != null ? o2.getSortOrder() : 0;
                return Integer.compare(s1, s2);
            });

            for (McpToolProtocolConfigVO.ProtocolMapping child : children) {
                // 递归处理，支持任意深度的嵌套对象结构
                props.put(child.getFieldName(), buildProperty(child, childrenMap));
                if (Integer.valueOf(1).equals(child.getIsRequired())) {
                    reqs.add(child.getFieldName());
                }
            }

            property.put("properties", props);
            if (!reqs.isEmpty()) {
                property.put("required", reqs);
            }
        }

        return property;
    }
}