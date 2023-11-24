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
@TableName("report_task")
public class ReportTaskEntity extends InfraBaseEntity {
	/**
	 * 任务名称
	 */
	@ColumnType(length = 100)
	@ColumnComment("任务名称")
	@TableField("task_name")
	private String taskName;

	/**
	 * 任务内容
	 */
	@ColumnType(MySqlTypeConstant.TEXT)
	@ColumnComment("任务内容")
	@TableField("task_content")
	private String taskContent;

	/**
	 * 任务状态
	 */
	@ColumnType(length = 20)
	@ColumnComment("任务状态")
	@TableField("task_status")
	private String taskStatus;

	/**
	 * 任务时间
	 */
	@ColumnType(value = MySqlTypeConstant.DATETIME, length = 18)
	@ColumnComment("任务时间")
	@TableField("task_time")
	private Date taskTime;

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
