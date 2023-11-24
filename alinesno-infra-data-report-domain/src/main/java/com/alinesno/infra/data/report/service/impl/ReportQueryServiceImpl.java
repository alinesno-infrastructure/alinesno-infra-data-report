package com.alinesno.infra.data.report.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alinesno.infra.data.report.entity.ReportQueryEntity;
import com.alinesno.infra.data.report.mapper.ReportQueryMapper;
import com.alinesno.infra.data.report.service.IReportQueryService;
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
public class ReportQueryServiceImpl extends IBaseServiceImpl<ReportQueryEntity, ReportQueryMapper> implements IReportQueryService {

	// 日志记录
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(ReportQueryServiceImpl.class);

}
