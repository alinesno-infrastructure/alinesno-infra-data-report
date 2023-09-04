package com.alinesno.infra.data.report.service;

import com.alinesno.infra.common.facade.services.IBaseService;
import com.alinesno.infra.data.report.entity.ReportDataEntity;

import java.io.File;
import java.io.IOException;

public interface IReportDataService extends IBaseService<ReportDataEntity> {
    void reportExcel(File file) throws IOException;
}
