package com.alinesno.infra.data.report.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alinesno.infra.data.report.entity.RecycleBinEntity;
import com.alinesno.infra.data.report.mapper.RecycleBinMapper;
import com.alinesno.infra.data.report.service.IRecycleBinService;
import com.alinesno.infra.common.core.service.impl.IBaseServiceImpl;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author LuoXiaoDong
 * @version 1.0.0
 */
@Service
public class RecycleBinServiceImpl extends IBaseServiceImpl<RecycleBinEntity, RecycleBinMapper> implements IRecycleBinService {

	// 日志记录
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(RecycleBinServiceImpl.class);

}
