package com.alinesno.infra.data.report.vo;

/**
 * 功能名： 【今日上报统计】
 */

public class FileReportTodayStats  {

    /**
     * 今日上报文件总数
     */
    private int reportTotal = 0 ;

    /**
    * 今日上报成功文件数
    */
    private int reportSuccess = 0 ;


    /**
    * 今日上报失败文件数
    */
    private int reportFail = 0 ;

    /**
     * 今日上报文件，正在上报中
     */
    private int reportSending = 0 ;



    /**
    * 今日上传文件文件，没有上报的文件或者上报异常的文件
    */
    private int reportDeal = 0 ;


    /**
     * 今日上传文件文件，上报时，校验失败的
     */
    private int reportCheckFail = 0 ;

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

    public int getReportDeal() {
        return reportDeal;
    }

    public void setReportDeal(int reportDeal) {
        this.reportDeal = reportDeal;
    }

    public int getReportSending() {
        return reportSending;
    }

    public void setReportSending(int reportSending) {
        this.reportSending = reportSending;
    }

    public int getReportCheckFail() {
        return reportCheckFail;
    }

    public void setReportCheckFail(int reportCheckFail) {
        this.reportCheckFail = reportCheckFail;
    }
}
