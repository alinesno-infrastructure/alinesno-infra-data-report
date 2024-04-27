package com.alinesno.infra.data.report.dto;

import lombok.Data;

//数据上报时模型信息
@Data
public class ModelDTO {

    private String id;
    private String fileId;
    private String fileName;
//    private String extendName;

    private String kafkaTopice;
    private String messageType;

//    private String storageFileFullName;
    private String storageFileSize;

}
