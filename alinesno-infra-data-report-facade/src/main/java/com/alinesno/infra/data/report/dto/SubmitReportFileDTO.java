package com.alinesno.infra.data.report.dto;

import java.util.Date;

//修改上报状态DTO
public class SubmitReportFileDTO {
    //用户文件id
    private String userFileId;

    //校验是否通过;0-否,1-是,2-刚上报,未校验   reportStatus为 2   ifCheck为0 ，上报异常校验不通过
    private int ifCheck;

    //上报状态;0-待上报,1-上报成功,2-上报异常,3-发送中
    //上报状态
    private int reportStatus;

    private Date reportTime;

    private String messageType;

    private String errorMsg;

    public SubmitReportFileDTO(String userFileId, Integer reportStatus) {
        this.userFileId = userFileId;
        this.reportStatus = reportStatus;
    }

    public SubmitReportFileDTO(String userFileId, int ifCheck, int reportStatus, Date reportTime,String messageType,String errorMsg) {
        this.userFileId = userFileId;
        this.ifCheck = ifCheck;
        this.reportStatus = reportStatus;
        this.reportTime = reportTime;
        this.messageType = messageType;
        this.errorMsg = errorMsg;
    }

    public String getUserFileId() {
        return userFileId;
    }

    public void setUserFileId(String userFileId) {
        this.userFileId = userFileId;
    }

    public int getReportStatus() {
        return reportStatus;
    }

    public void setReportStatus(Integer reportStatus) {
        this.reportStatus = reportStatus;
    }

    public Date getReportTime() {
        return reportTime;
    }

    public void setReportTime(Date reportTime) {
        this.reportTime = reportTime;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public int getIfCheck() {
        return ifCheck;
    }

    public void setIfCheck(int ifCheck) {
        this.ifCheck = ifCheck;
    }

    public void setReportStatus(int reportStatus) {
        this.reportStatus = reportStatus;
    }
}
