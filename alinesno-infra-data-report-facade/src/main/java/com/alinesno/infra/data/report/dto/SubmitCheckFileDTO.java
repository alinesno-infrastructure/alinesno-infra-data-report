package com.alinesno.infra.data.report.dto;


import java.util.Date;

//修改上报状态DTO
public class SubmitCheckFileDTO {
    //用户文件id
    private String userFileId;

    //校验文件名称
    private String checkFileName;

    //校验文件格式
    private String checkExtendName;

    //校验文件格式
    private String checkFileFath;

    private Date reportTime;

    public SubmitCheckFileDTO(String userFileId, String checkFileName, String checkExtendName) {
        this.userFileId = userFileId;
        this.checkFileName = checkFileName;
        this.checkExtendName = checkExtendName;
    }

    public SubmitCheckFileDTO(String userFileId, String checkFileName, String checkExtendName, Date reportTime) {
        this.userFileId = userFileId;
        this.checkFileName = checkFileName;
        this.checkExtendName = checkExtendName;
        this.reportTime = reportTime;
    }

    public SubmitCheckFileDTO(String userFileId, String checkFileName, String checkExtendName, String checkFileFath, Date reportTime) {
        this.userFileId = userFileId;
        this.checkFileName = checkFileName;
        this.checkExtendName = checkExtendName;
        this.checkFileFath = checkFileFath;
        this.reportTime = reportTime;
    }

    public String getUserFileId() {
        return userFileId;
    }

    public void setUserFileId(String userFileId) {
        this.userFileId = userFileId;
    }

    public String getCheckFileName() {
        return checkFileName;
    }

    public void setCheckFileName(String checkFileName) {
        this.checkFileName = checkFileName;
    }

    public String getCheckExtendName() {
        return checkExtendName;
    }

    public void setCheckExtendName(String checkExtendName) {
        this.checkExtendName = checkExtendName;
    }

    public Date getReportTime() {
        return reportTime;
    }

    public void setReportTime(Date reportTime) {
        this.reportTime = reportTime;
    }

    public String getCheckFileFath() {
        return checkFileFath;
    }

    public void setCheckFileFath(String checkFileFath) {
        this.checkFileFath = checkFileFath;
    }
}
