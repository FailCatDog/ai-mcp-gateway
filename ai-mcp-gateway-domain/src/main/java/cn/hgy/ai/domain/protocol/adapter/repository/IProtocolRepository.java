package cn.hgy.ai.domain.protocol.adapter.repository;

import cn.hgy.ai.domain.protocol.model.valobj.http.HTTPProtocolVO;

import java.util.List;

/**
 * 协议仓储服务接口
 *
 * @author orange.huang
 * 2026/3/13 08:21
 */
public interface IProtocolRepository {

    List<Long> saveHttpProtocolAndMapping(List<HTTPProtocolVO> httpProtocolVOS);

}
