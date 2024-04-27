package com.alinesno.infra.data.report.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.alinesno.infra.common.facade.mapper.entity.InfraBaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.ColumnComment;
import com.gitee.sunchenbin.mybatis.actable.annotation.ColumnType;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
import lombok.Data;

/**
 * 功能名： 发送kafka失败时，保存消息数据表： message_fail 表备注：
 *
 * @author paul
 * @date 2024-03-24 17:55:47
 */
@Data
@TableName("message_fail")
public class MessageFailEntity extends InfraBaseEntity {
	private static final long serialVersionUID = 1L;
	// fields

	/**
	 * 上报文件
	 */
	@ColumnComment("上报文件id")
	@Excel(name = "上报文件id")
	@ColumnType(length = 64)
	@TableField("file_report_id")
	private String fileReportId;

	/**
	 * 业务模型
	 */
	@ColumnComment("模型id")
	@Excel(name = "模型id")
	@ColumnType(length = 64)
	@TableField("model_id")
	private String modelId;


	/**
	 * 主题
	 */
	@ColumnComment("主题")
	@Excel(name = "主题")
	@ColumnType(length = 64)
	@TableField("topic")
	private String topic;

	/**
	 * 消息内容
	 */
	@ColumnComment("消息内容")
	@Excel(name = "消息内容")
	@ColumnType(value = MySqlTypeConstant.LONGTEXT)
	@TableField("data")
	private String data;

	/**
	 * 状态
	 */
	@ColumnComment("状态")
	@Excel(name = "状态")
	@ColumnType(length = 4)
	@TableField("status")
	private String status;

	/**
	 * 重试次数
	 */
	@ColumnComment("重试次数")
	@Excel(name = "重试次数")
	@ColumnType(length = 0)
	@TableField("retry_count")
	private Long retryCount = 0l ;  //设置默认值为0


	@ColumnComment("异常消息")
	@Excel(name = "异常消息")
	@ColumnType(length = 2048)
	@TableField("error_msg")
	private String errorMsg;

}
