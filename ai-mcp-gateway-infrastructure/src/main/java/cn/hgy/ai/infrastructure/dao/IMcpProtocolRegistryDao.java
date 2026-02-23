package cn.hgy.ai.infrastructure.dao;

import cn.hgy.ai.infrastructure.dao.po.McpProtocolRegistryPO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-02-22
 * @Description:
 */
@Mapper
public interface IMcpProtocolRegistryDao {

    int insert(McpProtocolRegistryPO po);

    int deleteById(Long id);

    int updateById(McpProtocolRegistryPO po);

    McpProtocolRegistryPO queryById(Long id);

    List<McpProtocolRegistryPO> queryAll();
}
