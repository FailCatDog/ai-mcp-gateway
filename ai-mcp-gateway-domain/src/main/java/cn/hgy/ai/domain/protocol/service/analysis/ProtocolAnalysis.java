package cn.hgy.ai.domain.protocol.service.analysis;

import cn.hgy.ai.domain.protocol.model.entity.AnalysisCommandEntity;
import cn.hgy.ai.domain.protocol.model.valobj.enums.AnalysisTypeEnum;
import cn.hgy.ai.domain.protocol.model.valobj.http.HTTPProtocolVO;
import cn.hgy.ai.domain.protocol.service.IProtocolAnalysis;
import cn.hgy.ai.domain.protocol.service.analysis.strategy.IProtocolAnalysisStrategy;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-03-03
 * @Description: 协议解析服务实现，基于 Swagger OpenAPI JSON 解析 HTTP 协议映射
 */
@Slf4j
@Service
public class ProtocolAnalysis implements IProtocolAnalysis {

    private final Map<String, IProtocolAnalysisStrategy> protocolAnalysisStrategyMap;

    public ProtocolAnalysis(Map<String, IProtocolAnalysisStrategy> protocolAnalysisStrategyMap) {
        this.protocolAnalysisStrategyMap = protocolAnalysisStrategyMap;
    }

    /**
     * {
     *   "openapi": "3.0.3",
     *   "info": {
     *     "title": "电商订单API",
     *     "description": "订单创建、查询、取消接口",
     *     "version": "1.0.0"
     *   },
     *   "servers": [
     *     {
     *       "url": "https://api.shop.com/v1",
     *       "description": "生产环境"
     *     }
     *   ],
     *   "tags": [
     *     {
     *       "name": "订单管理",
     *       "description": "订单相关操作"
     *     }
     *   ],
     *   "paths": {
     *     "/orders/{orderId}": {
     *       "get": {
     *         "tags": ["订单管理"],
     *         "summary": "根据ID查询订单",
     *         "operationId": "getOrderById",
     *         "parameters": [
     *           {
     *             "name": "orderId",
     *             "in": "path",
     *             "required": true,
     *             "description": "订单ID",
     *             "schema": {
     *               "type": "integer",
     *               "format": "int64"
     *             }
     *           },
     *           {
     *             "name": "token",
     *             "in": "header",
     *             "required": true,
     *             "description": "登录令牌",
     *             "schema": {
     *               "type": "string"
     *             }
     *           }
     *         ],
     *         "responses": {
     *           "200": {
     *             "description": "查询成功",
     *             "content": {
     *               "application/json": {
     *                 "schema": {
     *                   "$ref": "#/components/schemas/Order"
     *                 }
     *               }
     *             }
     *           },
     *           "404": {
     *             "description": "订单不存在",
     *             "content": {
     *               "application/json": {
     *                 "schema": {
     *                   "$ref": "#/components/schemas/BaseResponse"
     *                 }
     *               }
     *             }
     *           }
     *         }
     *       }
     *     },
     *     "/orders": {
     *       "post": {
     *         "tags": ["订单管理"],
     *         "summary": "创建订单",
     *         "operationId": "createOrder",
     *         "requestBody": {
     *           "required": true,
     *           "content": {
     *             "application/json": {
     *               "schema": {
     *                 "$ref": "#/components/schemas/CreateOrderRequest"
     *               }
     *             }
     *           }
     *         },
     *         "responses": {
     *           "201": {
     *             "description": "创建成功",
     *             "content": {
     *               "application/json": {
     *                 "schema": {
     *                   "$ref": "#/components/schemas/BaseResponse"
     *                 }
     *               }
     *             }
     *           }
     *         }
     *       }
     *     }
     *   },
     *   "components": {
     *     "schemas": {
     *       "BaseResponse": {
     *         "type": "object",
     *         "properties": {
     *           "code": {
     *             "type": "integer",
     *             "description": "响应码（200成功）"
     *           },
     *           "msg": {
     *             "type": "string",
     *             "description": "响应信息"
     *           },
     *           "data": {
     *             "type": "object",
     *             "description": "返回数据"
     *           }
     *         },
     *         "required": ["code", "msg"]
     *       },
     *       "Order": {
     *         "type": "object",
     *         "properties": {
     *           "orderId": {
     *             "type": "integer",
     *             "format": "int64",
     *             "description": "订单ID"
     *           },
     *           "orderNo": {
     *             "type": "string",
     *             "description": "订单编号"
     *           },
     *           "amount": {
     *             "type": "number",
     *             "format": "double",
     *             "description": "订单金额"
     *           },
     *           "status": {
     *             "type": "string",
     *             "enum": ["PAYED", "UNPAYED", "CANCELED"],
     *             "description": "订单状态"
     *           }
     *         },
     *         "required": ["orderId", "orderNo"]
     *       },
     *       "CreateOrderRequest": {
     *         "type": "object",
     *         "properties": {
     *           "productId": {
     *             "type": "integer",
     *             "description": "商品ID"
     *           },
     *           "count": {
     *             "type": "integer",
     *             "description": "购买数量"
     *           },
     *           "addressId": {
     *             "type": "integer",
     *             "description": "收货地址ID"
     *           }
     *         },
     *         "required": ["productId", "count"]
     *       }
     *     }
     *   }
     * }
     * @param commandEntity
     * @return
     */
    @Override
    public List<HTTPProtocolVO> doAnalysis(AnalysisCommandEntity commandEntity) {
        log.info("协议解析请求 endpoints:{} openApiJson:{}", JSON.toJSONString(commandEntity.getEndpoints()), commandEntity.getOpenApiJson());

        List<HTTPProtocolVO> list = new ArrayList<>();
        try {
            JSONObject root = JSON.parseObject(commandEntity.getOpenApiJson());
            String baseUrl = root.getJSONArray("servers").getJSONObject(0).getString("url");
            JSONObject paths = root.getJSONObject("paths");
            JSONObject schemas = root.getJSONObject("components").getJSONObject("schemas");

            List<String> endpoints = commandEntity.getEndpoints();
            if (null == endpoints || endpoints.isEmpty()) return list;

            for (String endpoint : endpoints) {
                JSONObject pathItem = paths.getJSONObject(endpoint);
                if (pathItem == null) continue;

                String method = detectMethod(pathItem);
                JSONObject operation = pathItem.getJSONObject(method);

                HTTPProtocolVO vo = new HTTPProtocolVO();
                vo.setHttpUrl(baseUrl + endpoint);
                vo.setHttpMethod(method);
                vo.setHttpHeaders(JSON.toJSONString(new HashMap<>() {{
                    put("Content-Type", "application/json");
                }}));
                vo.setTimeout(30000);

                List<HTTPProtocolVO.ProtocolMapping> mappings = new ArrayList<>();

                // 枚举策略动作处理
                AnalysisTypeEnum.SwaggerAnalysisAction analysisAction = AnalysisTypeEnum.SwaggerAnalysisAction.get(operation);
                IProtocolAnalysisStrategy strategy = protocolAnalysisStrategyMap.get(analysisAction.getCode());
                strategy.doAnalysis(operation, schemas, mappings);

                vo.setMappings(mappings);
                list.add(vo);
            }

        } catch (Exception e) {
            log.error("协议解析失败 endpoints:{} openApiJson:{}", JSON.toJSONString(commandEntity.getEndpoints()), commandEntity.getOpenApiJson(), e);
        }

        return list;
    }

    private String detectMethod(JSONObject pathItem) {
        if (pathItem.containsKey("post")) return "post";
        if (pathItem.containsKey("get")) return "get";
        if (pathItem.containsKey("put")) return "put";
        if (pathItem.containsKey("delete")) return "delete";
        return "post";
    }

}
