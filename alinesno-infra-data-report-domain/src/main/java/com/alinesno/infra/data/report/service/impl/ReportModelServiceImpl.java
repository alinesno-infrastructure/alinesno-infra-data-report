package com.alinesno.infra.data.report.service.impl;

import com.alinesno.infra.common.core.service.impl.IBaseServiceImpl;
import com.alinesno.infra.data.report.entity.ReportModelEntity;
import com.alinesno.infra.data.report.mapper.ReportModelMapper;
import com.alinesno.infra.data.report.service.IReportModelService;
import org.springframework.stereotype.Service;

@Service
public class ReportModelServiceImpl extends IBaseServiceImpl<ReportModelEntity , ReportModelMapper> implements IReportModelService {
}
