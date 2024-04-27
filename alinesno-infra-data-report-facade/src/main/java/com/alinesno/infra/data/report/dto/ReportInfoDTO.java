package com.alinesno.infra.data.report.dto;

//数据上报时接口参数
public class ReportInfoDTO {

    private String id;
    private String modelId;
    private String fileName;
    private String extendName;
    private String fileUrl;
    private String messageType;
    private String kafkaTopic;
    private String oldCheckFileName;
    private String oldCheckExtendName;
    private Long operatorId;
    private String local_path;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getExtendName() {
        return extendName;
    }

    public void setExtendName(String extendName) {
        this.extendName = extendName;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getKafkaTopic() {
        return kafkaTopic;
    }

    public void setKafkaTopic(String kafkaTopic) {
        this.kafkaTopic = kafkaTopic;
    }

    public String getOldCheckFileName() {
        return oldCheckFileName;
    }

    public void setOldCheckFileName(String oldCheckFileName) {
        this.oldCheckFileName = oldCheckFileName;
    }

    public String getOldCheckExtendName() {
        return oldCheckExtendName;
    }

    public void setOldCheckExtendName(String oldCheckExtendName) {
        this.oldCheckExtendName = oldCheckExtendName;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    public String getLocal_path() {
        return local_path;
    }

    public void setLocal_path(String local_path) {
        this.local_path = local_path;
    }
}
