package com.alinesno.infra.data.report.service.impl;

import com.alinesno.infra.common.core.service.impl.IBaseServiceImpl;
import com.alinesno.infra.data.report.entity.ReportDataEntity;
import com.alinesno.infra.data.report.mapper.ReportDataMapper;
import com.alinesno.infra.data.report.service.IReportDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;

@Service
public class ReportDataServiceImpl extends IBaseServiceImpl<ReportDataEntity , ReportDataMapper> implements IReportDataService {

    @Autowired
    private ReportDataMapper reportDataMapper;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public void reportExcel(File file) throws IOException {
        // 保存到数据库
        saveToDatabase(file);

        // 发送到Kafka消息队列
        sendToKafka(file);
    }

    private void saveToDatabase(File file) {
        // 根据需求实现将文件保存到数据库的逻辑
        try {
            byte[] fileData = Files.readAllBytes(file.toPath());
            ReportDataEntity reportDataEntity = new ReportDataEntity();

//            reportDataEntity.setFileName(file.getName());
//            reportDataEntity.setFileData(fileData);

            reportDataMapper.insert(reportDataEntity);
        } catch (IOException e) {
            // 处理文件读取异常
            e.printStackTrace();
        }
    }

    private void sendToKafka(File file) {
        // 发送到Kafka消息队列的逻辑
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                kafkaTemplate.send("file-uploads", line);
            }
        } catch (IOException e) {
            // 处理文件读取异常
            e.printStackTrace();
        }
    }
}
