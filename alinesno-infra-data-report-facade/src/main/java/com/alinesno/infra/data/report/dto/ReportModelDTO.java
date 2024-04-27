package com.alinesno.infra.data.report.dto;

//数据上报时模型信息
public class ReportModelDTO {

    private String modelID;
    private String kafkaTopice;
    private Long columnNum;
    private String columnCnName;
    private String columnName;


    public String getModelID() {
        return modelID;
    }

    public void setModelID(String modelID) {
        this.modelID = modelID;
    }

    public String getKafkaTopice() {
        return kafkaTopice;
    }

    public void setKafkaTopice(String kafkaTopice) {
        this.kafkaTopice = kafkaTopice;
    }

    public Long getColumnNum() {
        return columnNum;
    }

    public void setColumnNum(Long columnNum) {
        this.columnNum = columnNum;
    }

    public String getColumnCnName() {
        return columnCnName;
    }

    public void setColumnCnName(String columnCnName) {
        this.columnCnName = columnCnName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }
}
