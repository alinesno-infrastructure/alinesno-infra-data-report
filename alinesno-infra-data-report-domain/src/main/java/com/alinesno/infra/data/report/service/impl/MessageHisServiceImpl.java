package com.alinesno.infra.data.report.service.impl;

import com.alinesno.infra.common.core.service.impl.IBaseServiceImpl;
import com.alinesno.infra.data.report.entity.MessageHisEntity;
import com.alinesno.infra.data.report.mapper.MessageHisMapper;
import com.alinesno.infra.data.report.service.IMessageHisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 【保存消息】Service业务层处理
 *
 * @author paul
 * @date 2022-05-09 09:33:44
 */
@Service
public class MessageHisServiceImpl extends IBaseServiceImpl<MessageHisEntity, MessageHisMapper>
		implements IMessageHisService {
	// 日志记录
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(MessageHisServiceImpl.class);


}
