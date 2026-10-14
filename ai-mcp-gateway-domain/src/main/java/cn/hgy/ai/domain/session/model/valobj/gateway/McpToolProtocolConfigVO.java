package cn.hgy.ai.domain.session.model.valobj.gateway;

import lombok.*;

import java.util.List;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-03-04
 * @Description: 协议配置
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class McpToolProtocolConfigVO {

    /**
     * 协议类型：http / ai_model
     */
    private String protocolType;

    /**
     * 请求协议配置
     */
    private HTTPConfig httpConfig;

    /**
     * AI 模型协议配置
     */
    private AIModelConfig aiModelConfig;

    /**
     * 请求协议映射
     * 注：AI 模型工具注册时，只需提供一条 prompt 字段的 mapping，即可复用现有 schema 构建流程
     */
    private List<ProtocolMapping> requestProtocolMappings;

    @Data
    public static class AIModelConfig {
        /**
         * 模型类型：openai / ollama
         */
        private String modelType;
        /**
         * 系统提示词，告知模型角色定位
         */
        private String prompt;
    }

    @Data
    public static class HTTPConfig {
        private String httpUrl;
        private String httpHeaders;
        private String httpMethod;
        private Integer timeout;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProtocolMapping {
        /**
         * 映射类型：request-请求参数映射，response-响应数据映射
         */
        private String mappingType;
        /**
         * 父级路径（如：xxxRequest01，用于构建嵌套结构，根节点为NULL）
         */
        private String parentPath;
        /**
         * 字段名称（如：city、company、name）
         */
        private String fieldName;
        /**
         * MCP完整路径（如：xxxRequest01.city、xxxRequest01.company.name）
         */
        private String mcpPath;
        /**
         * MCP数据类型：string/number/boolean/object/array
         */
        private String mcpType;
        /**
         * MCP字段描述
         */
        private String mcpDesc;
        /**
         * 是否必填：0-否，1-是（用于生成required数组）
         */
        private Integer isRequired;
        /**
         * 排序顺序（同级字段排序）
         */
        private Integer sortOrder;
    }
}
