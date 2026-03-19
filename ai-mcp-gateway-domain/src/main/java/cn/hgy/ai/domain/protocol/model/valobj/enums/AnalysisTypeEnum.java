package cn.hgy.ai.domain.protocol.model.valobj.enums;

import cn.hgy.ai.types.enums.ResponseCode;
import cn.hgy.ai.types.exception.AppException;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-03-03
 * @Description: 协议解析类型枚举，定义 Swagger 解析动作策略映射
 */
@Getter
public enum AnalysisTypeEnum {

    swagger,

    ;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public enum SwaggerAnalysisAction {

        requestBodyAnalysis("requestBodyAnalysis", "解析对象"),
        parametersAnalysis("parametersAnalysis", "解析属性"),

        ;

        private String code;
        private String info;

        public static SwaggerAnalysisAction get(JSONObject operation) {

            JSONObject requestBody = operation.getJSONObject("requestBody");
            JSONArray parameters = operation.getJSONArray("parameters");

            if (null != requestBody) {
                return requestBodyAnalysis;
            }

            if (null != parameters) {
                return parametersAnalysis;
            }

            throw new AppException(ResponseCode.ENUM_NOT_FOUND.getCode(), ResponseCode.ENUM_NOT_FOUND.getInfo());
        }

    }

}
