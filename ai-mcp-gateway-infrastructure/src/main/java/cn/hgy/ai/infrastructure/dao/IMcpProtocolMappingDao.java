package cn.hgy.ai.infrastructure.dao;

import cn.hgy.ai.infrastructure.dao.po.McpProtocolMappingPO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-02-22
 * @Description:
 */
@Mapper
public interface IMcpProtocolMappingDao {
    int insert(McpProtocolMappingPO po);

    int deleteById(Long id);

    int updateById(McpProtocolMappingPO po);

    McpProtocolMappingPO queryById(Long id);

    List<McpProtocolMappingPO> queryAll();

    List<McpProtocolMappingPO> queryMcpGatewayToolConfigList(McpProtocolMappingPO mcpProtocolMappingPO);
}
