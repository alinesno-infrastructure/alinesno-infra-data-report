package com.alinesno.infra.data.report.entity;

import java.util.Date;
import com.alinesno.infra.common.facade.mapper.entity.InfraBaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.ColumnComment;
import com.gitee.sunchenbin.mybatis.actable.annotation.ColumnType;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
import lombok.Data;


/**
 * <p>
 *
 * </p>
 *
 * @author LuoXiaoDong
 * @version 1.0.0
 */
@Data
@TableName("report_failure")
public class ReportFailureEntity extends InfraBaseEntity {
	/**
	 * 失败内容
	 */
	@ColumnType(MySqlTypeConstant.TEXT)
	@ColumnComment("失败内容")
	@TableField("failure_content")
	private String failureContent;

	/**
	 * 失败原因
	 */
	@ColumnType(MySqlTypeConstant.TEXT)
	@ColumnComment("失败原因")
	@TableField("failure_reason")
	private String failureReason;

	/**
	 * 是否需要重新上报
	 */
	@ColumnType(length = 1)
	@ColumnComment("是否需要重新上报")
	@TableField("need_retry")
	private int needRetry;

	/**
	 * 创建时间
	 */
	@ColumnType(value = MySqlTypeConstant.DATETIME, length = 18)
	@ColumnComment("创建时间")
	@TableField("create_time")
	private Date createTime;

	/**
	 * 删除标识：0-未删除，1-已删除
	 */
	@ColumnType(length = 1)
	@ColumnComment("删除标识：0-未删除，1-已删除")
	@TableField("is_deleted")
	private int isDeleted;


}
