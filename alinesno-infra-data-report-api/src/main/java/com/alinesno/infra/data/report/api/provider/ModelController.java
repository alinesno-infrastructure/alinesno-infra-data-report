package com.alinesno.infra.data.report.api.provider;

import com.alinesno.infra.common.facade.response.AjaxResult;
import com.alinesno.infra.data.report.api.dto.ReportModelDTO;
import com.alinesno.infra.data.report.entity.ReportModelEntity;
import com.alinesno.infra.data.report.service.IReportModelService;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * 模型实体控制层
 */
@RestController
@RequestMapping("/report-models")
@Validated
public class ModelController {

    private final IReportModelService reportModelService;

    @Autowired
    public ModelController(IReportModelService reportModelService) {
        this.reportModelService = reportModelService;
    }

    @PostMapping
    public AjaxResult saveReportModels(@RequestBody @Valid List<ReportModelDTO> reportModels) {
        List<ReportModelEntity> reportModelEntities = convertToEntities(reportModels);
        reportModelService.saveBatch(reportModelEntities);
        return AjaxResult.success("Report models saved successfully.");
    }

    private List<ReportModelEntity> convertToEntities(List<ReportModelDTO> reportModels) {
        List<ReportModelEntity> entities = new ArrayList<>();
        for (ReportModelDTO dto : reportModels) {
            ReportModelEntity entity = new ReportModelEntity();
            BeanUtils.copyProperties(entity, dto);
            // 继续设置其他字段的值
            entities.add(entity);
        }
        return entities;
    }

}
