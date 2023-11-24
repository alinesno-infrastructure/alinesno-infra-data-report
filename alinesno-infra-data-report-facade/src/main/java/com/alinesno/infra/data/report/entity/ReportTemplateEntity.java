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
@TableName("report_template")
public class ReportTemplateEntity extends InfraBaseEntity {
	/**
	 * 模板名称
	 */
	@ColumnType(length = 100)
	@ColumnComment("模板名称")
	@TableField("template_name")
	private String templateName;

	/**
	 * 模板字段要求
	 */
	@ColumnType(MySqlTypeConstant.TEXT)
	@ColumnComment("模板字段要求")
	@TableField("template_fields")
	private String templateFields;

	/**
	 * 模板类型
	 */
	@ColumnType(length = 50)
	@ColumnComment("模板类型")
	@TableField("template_type")
	private String templateType;

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
