package com.alinesno.infra.data.report.vo;

/**
 * 功能名： 【今日消息分布】
 */

public class FileReportMsgStats {

    /**
     * 今日上报文件总数
     */
    private int successRow;

    /**
    * 今日上报成功文件数
    */
    private int failRow;


    //getter and setter


    public int getSuccessRow() {
        return successRow;
    }

    public void setSuccessRow(int successRow) {
        this.successRow = successRow;
    }

    public int getFailRow() {
        return failRow;
    }

    public void setFailRow(int failRow) {
        this.failRow = failRow;
    }
}
