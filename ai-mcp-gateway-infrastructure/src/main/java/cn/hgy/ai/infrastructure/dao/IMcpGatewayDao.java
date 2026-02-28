package cn.hgy.ai.infrastructure.dao;

import cn.hgy.ai.infrastructure.dao.po.McpGatewayPO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-02-22
 * @Description:
 */
@Mapper
public interface IMcpGatewayDao {

    int insert(McpGatewayPO po);

    int deleteById(Long id);

    int updateById(McpGatewayPO po);

    McpGatewayPO queryById(Long id);

    List<McpGatewayPO> queryAll();

    McpGatewayPO queryMcpGatewayByGatewayId(String gatewayId);
}
