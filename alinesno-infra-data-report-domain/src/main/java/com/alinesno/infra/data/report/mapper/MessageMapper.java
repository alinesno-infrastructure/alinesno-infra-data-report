package com.alinesno.infra.data.report.mapper;

import com.alinesno.infra.common.facade.mapper.repository.IBaseMapper;
import com.alinesno.infra.data.report.entity.MessageEntity;
import org.springframework.stereotype.Repository;

/**
 * 【保存消息】Mapper接口
 *
 * @author paul
 * @date 2024年3月10日
 */

@Repository
public interface MessageMapper extends IBaseMapper<MessageEntity> {

}
