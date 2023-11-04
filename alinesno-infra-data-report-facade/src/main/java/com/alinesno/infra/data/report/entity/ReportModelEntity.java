package com.alinesno.infra.data.report.entity;

import com.alinesno.infra.common.facade.mapper.entity.InfraBaseEntity;
import com.gitee.sunchenbin.mybatis.actable.annotation.ColumnComment;
import com.gitee.sunchenbin.mybatis.actable.annotation.ColumnType;
import lombok.Data;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 上报表单模型
 */
@TableName("report_model")
@Data
public class ReportModelEntity extends InfraBaseEntity {

    @TableField("business_name")
	@ColumnType(length=255)
	@ColumnComment("字段用途的描述")
    private String businessName; // 业务名称，字段用途的描述

    @TableField("name")
	@ColumnType(length=255)
	@ColumnComment("姓名")
    private String name; // 字段名称

    @TableField("field")
	@ColumnType(length=255)
	@ColumnComment("字段")
    private String field; // 字段标识

    @TableField("min_length")
	@ColumnType(length=255)
	@ColumnComment("最小长度")
    private int minLength; // 最小长度，字段值的最小长度限制

    @TableField("max_length")
	@ColumnType(length=255)
	@ColumnComment("最大长度")
    private int maxLength; // 最大长度，字段值的最大长度限制

    @TableField("has_null")
	@ColumnType(length=1)
	@ColumnComment("是否为空")
    private String hasNull; // 是否允许为空，表示字段值是否可以为空

    @TableField("key")
	@ColumnType(length=255)
	@ColumnComment("是否为主字段")
    private String key; // 是否为主字段，表示是否为主要字段，不能为空，不存在则自动填充

    @TableField("type")
	@ColumnType(length=255)
	@ColumnComment("类型")
    private String type; // 字段类型，表示字段的数据类型

    @TableField("value")
	@ColumnType(length=255)
	@ColumnComment("value")
    private String value; // 字段值，表示字段的当前值
}
