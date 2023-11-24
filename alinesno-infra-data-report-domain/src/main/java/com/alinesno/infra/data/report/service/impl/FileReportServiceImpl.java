package com.alinesno.infra.data.report.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alinesno.infra.data.report.entity.FileReportEntity;
import com.alinesno.infra.data.report.mapper.FileReportMapper;
import com.alinesno.infra.data.report.service.IFileReportService;
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
public class FileReportServiceImpl extends IBaseServiceImpl<FileReportEntity, FileReportMapper> implements IFileReportService {

	// 日志记录
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(FileReportServiceImpl.class);

}
