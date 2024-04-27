package com.alinesno.infra.data.report.vo;

/**
 * 功能名： 【异常情况推送统计】
 */

public class FileReportAbnlStats {

    /**
     * 上报文件总数
     */
    private int reportTotal;

    /**
    * 上报成功文件数
    */
    private int reportSuccess;

    /**
    * 上报失败文件数
    */
    private int reportFail;



    //getter and setter


    public int getReportTotal() {
        return reportTotal;
    }

    public void setReportTotal(int reportTotal) {
        this.reportTotal = reportTotal;
    }

    public int getReportSuccess() {
        return reportSuccess;
    }

    public void setReportSuccess(int reportSuccess) {
        this.reportSuccess = reportSuccess;
    }

    public int getReportFail() {
        return reportFail;
    }

    public void setReportFail(int reportFail) {
        this.reportFail = reportFail;
    }

}
