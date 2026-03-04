package cn.hgy.ai.domain.session.service.message.handler.impl;

import cn.hgy.ai.domain.session.adapter.repository.ISessionRepository;
import cn.hgy.ai.domain.session.model.valobj.McpSchemaVO;
import cn.hgy.ai.domain.session.model.valobj.gateway.McpGatewayConfigVO;
import cn.hgy.ai.domain.session.model.valobj.gateway.McpGatewayToolConfigVO;
import cn.hgy.ai.domain.session.service.message.handler.IRequestHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-01-30
 * @Description: 返回工具
 */
@Slf4j
@Service("toolsListHandler")
@RequiredArgsConstructor
public class ToolsListHandler implements IRequestHandler {

    private final ISessionRepository repository;

    @Override
    public McpSchemaVO.JSONRPCResponse handle(String gatewayId, McpSchemaVO.JSONRPCRequest message) {

        // 1. 网关配置
        McpGatewayConfigVO mcpGatewayConfigVO = repository.queryMcpGatewayConfigByGatewayId(gatewayId);

        // 2. 查询网关下的工具列表配置
        List<McpGatewayToolConfigVO> mcpGatewayToolConfigVOS = repository.queryMcpGatewayToolConfigListByGatewayId(gatewayId);

        List<McpSchemaVO.Tool> tools = buildTools(mcpGatewayConfigVO, mcpGatewayToolConfigVOS);

        return new McpSchemaVO.JSONRPCResponse("2.0", message.id(), Map.of("tools", tools), null);
    }

    private List<McpSchemaVO.Tool> buildTools(McpGatewayConfigVO gatewayConfigVO, List<McpGatewayToolConfigVO> toolConfigs) {

        Map<Long, List<McpGatewayToolConfigVO>> toolsMap = toolConfigs.stream()
                .collect(Collectors.groupingBy(McpGatewayToolConfigVO::getToolId));

        List<McpSchemaVO.Tool> tools = new ArrayList<>();

        for (Map.Entry<Long, List<McpGatewayToolConfigVO>> entry:toolsMap.entrySet()) {
            Long toolId = entry.getKey();

            List<McpGatewayToolConfigVO> configs = entry.getValue();

            configs.sort(((o1, o2) -> {
                int s1 = o1.getSortOrder() != null ? o1.getSortOrder() : 0;
                int s2 = o2.getSortOrder() != null ? o2.getSortOrder() : 0;
                return Integer.compare(s1, s2);
            }));

            Map<String, List<McpGatewayToolConfigVO>> childrenMap = new HashMap<>();

            List<McpGatewayToolConfigVO> roots = new ArrayList<>();

            for (McpGatewayToolConfigVO config : configs) {
                if (config.getParentPath() == null) {
                    roots.add(config);
                }else {
                    childrenMap.computeIfAbsent(config.getParentPath(), k-> new ArrayList<>()).add(config);
                }
            }

            roots.sort(((o1, o2) -> {
                int s1 = o1.getSortOrder() != null ? o1.getSortOrder() : 0;
                int s2 = o2.getSortOrder() != null ? o2.getSortOrder() : 0;
                return Integer.compare(s1, s2);
            }));

            Map<String, Object> properties = new HashMap<>();
            List<String> required = new ArrayList<>();

            for (McpGatewayToolConfigVO root : roots) {
                properties.put(root.getFieldName(), buildProperty(root, childrenMap));
                if (Integer.valueOf(1).equals(root.getIsRequired())) {
                    required.add(root.getFieldName());
                }
            }

            String type = roots.size() == 1 ? roots.get(0).getMcpType() : "object";

            McpSchemaVO.JsonSchema inputSchema =  new McpSchemaVO.JsonSchema(
                    type,
                    properties,
                    required.isEmpty() ? null :required,
                    false,
                    null,
                    null
            );

            String name = "unknown-tool" +toolId;
            String desc = "";

            if (gatewayConfigVO != null && Objects.equals(gatewayConfigVO.getToolId(), toolId)) {
                name = gatewayConfigVO.getToolName();
                desc = gatewayConfigVO.getToolDesc();
            }

            tools.add(new McpSchemaVO.Tool(name, desc, inputSchema));

        }

        return tools;
    }

    private Map<String, Object> buildProperty(McpGatewayToolConfigVO current, Map<String, List<McpGatewayToolConfigVO>> childrenMap) {

        Map<String, Object> property = new HashMap<>();
        property.put("type", current.getMcpType());
        if (current.getMcpDesc() != null) {
            property.put("description", current.getMcpDesc());
        }

        List<McpGatewayToolConfigVO> children = childrenMap.get(current.getMcpPath());

        if (children != null && !children.isEmpty()) {
            Map<String, Object> props = new HashMap<>();

            List<String> reqs = new ArrayList<>();

            children.sort(((o1, o2) -> {
                int s1 = o1.getSortOrder() != null ? o1.getSortOrder() : 0;
                int s2 = o2.getSortOrder() != null ? o2.getSortOrder() : 0;
                return Integer.compare(s1, s2);
            }));

            for (McpGatewayToolConfigVO child :children) {
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