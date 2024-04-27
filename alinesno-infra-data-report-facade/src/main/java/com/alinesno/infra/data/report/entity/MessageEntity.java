package com.alinesno.infra.data.report.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.alinesno.infra.common.facade.mapper.entity.InfraBaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
import lombok.Data;
import com.gitee.sunchenbin.mybatis.actable.annotation.ColumnComment;
import com.gitee.sunchenbin.mybatis.actable.annotation.ColumnType;

/**
 * 功能名： 【保存消息】 数据表： message 表备注：
 *
 * @author paul
 * @date 2024-03-24 17:55:47
 */
@Data
@TableName("message")
public class MessageEntity extends InfraBaseEntity {
	private static final long serialVersionUID = 1L;
	// fields
	/**
	 * 业务模型ID
	 */
	@ColumnComment("业务模型ID")
	@Excel(name = "业务模型ID")
	@ColumnType(length = 64)
	@TableField("model_id")
	private String modelId;

	/**
	 * 上报文件ID
	 */
	@ColumnComment("上报文件ID")
	@Excel(name = "上报文件ID")
	@ColumnType(length = 64)
	@TableField("file_id")
	private String fileId;

	/**
	 * 消息主题
	 */
	@ColumnComment("消息主题")
	@Excel(name="消息主题")
	@ColumnType(length = 64)
	@TableField("topice")
	private String topice;

	/**
	 * 序号
	 */
	@ColumnComment("序号")
	@Excel(name = "序号")
	@TableField("file_index")
	private int fileIndex;

	/**
	 * 消息内容
	 */
	@ColumnComment("消息内容")
	@Excel(name = "消息内容")
	@ColumnType(value = MySqlTypeConstant.TEXT)
	@TableField("data")
	private String data;

	/**
	 * 状态
	 * @see com.alinesno.cloud.data.integration.enums.reportStatusEnum
	 */
	@ColumnComment("状态")
	@Excel(name = "状态")
	@ColumnType(length = 4)
	@TableField("report_status")
	private int reportStatus;

	/**
	 * 重试次数
	 */
	@ColumnComment("重试次数")
	@Excel(name = "重试次数")
	@ColumnType(length = 10)
	@TableField("retryCount")
	private int retryCount = 0 ;

	/**
	 * 超时时间
	 */
	@ColumnComment("超时时间")
	@Excel(name = "超时时间")
	@ColumnType(length = 19)
	@TableField("timeout")
	private Long timeout;


	@ColumnComment("异常消息")
	@Excel(name = "异常消息")
	@ColumnType(length = 2048)
	@TableField("error_msg")
	private String errorMsg;

	/**
	 * 目标库
	 */
	@ColumnComment("目标库")
	@Excel(name="目标库")
	@ColumnType(length = 1)
	@TableField("message_type")
	private Integer messageType;


	/**
	 * 发往数据总线
	 */
	@ColumnComment("发往数据总线")
	@Excel(name="发往数据总线")
	@ColumnType(length = 1)
	@TableField("if_send_bus")
	private Integer ifSendBus;


	public String getModelId() {
		return modelId;
	}

	public void setModelId(String modelId) {
		this.modelId = modelId;
	}

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public int getFileIndex() {
		return fileIndex;
	}

	public void setFileIndex(int fileIndex) {
		this.fileIndex = fileIndex;
	}

	public String getTopice() {
		return topice;
	}

	public void setTopice(String topice) {
		this.topice = topice;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public int getReportStatus() {
		return reportStatus;
	}

	public void setReportStatus(int reportStatus) {
		this.reportStatus = reportStatus;
	}

	public int getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}

	public Long getTimeout() {
		return timeout;
	}

	public void setTimeout(Long timeout) {
		this.timeout = timeout;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public Integer getMessageType() {
		return messageType;
	}

	public void setMessageType(Integer messageType) {
		this.messageType = messageType;
	}

	public Integer getIfSendBus() {
		return ifSendBus;
	}

	public void setIfSendBus(Integer ifSendBus) {
		this.ifSendBus = ifSendBus;
	}

	@Override
	public String toString() {
		return "MessageEntity{" +
				"modelId='" + modelId + '\'' +
				", fileId='" + fileId + '\'' +
				", topice='" + topice + '\'' +
				", fileIndex=" + fileIndex +
				", data='" + data + '\'' +
				", reportStatus=" + reportStatus +
				", retryCount=" + retryCount +
				", timeout=" + timeout +
				", errorMsg='" + errorMsg + '\'' +
				", messageType=" + messageType +
				", ifSendBus=" + ifSendBus +
				'}';
	}
}

