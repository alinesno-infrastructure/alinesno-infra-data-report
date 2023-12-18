package com.alinesno.infra.data.report.gateway.provider;

import com.alinesno.infra.common.facade.response.AjaxResult;
import com.alinesno.infra.data.report.service.IReportDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/report")
public class ReportController {

    private static final Logger log = LoggerFactory.getLogger(ReportController.class);

    @Autowired
    private IReportDataService reportService;

    @PostMapping("/excel")
    public AjaxResult reportExcel(MultipartFile file) {
        if (file.isEmpty()) {
            return AjaxResult.error("上传的文件为空");
        }

        try {
            // 将MultipartFile转换为File
            File convertedFile = convertMultipartFileToFile(file);

            // 调用报表服务进行Excel上报
            reportService.reportExcel(convertedFile);

            return AjaxResult.success();
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return AjaxResult.error("文件上传失败");
        }
    }

    private File convertMultipartFileToFile(MultipartFile multipartFile) throws IOException {
        File convertedFile = File.createTempFile("temp", null);
        multipartFile.transferTo(convertedFile);
        return convertedFile;
    }

}
