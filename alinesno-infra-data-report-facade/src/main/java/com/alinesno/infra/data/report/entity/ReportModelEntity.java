package com.alinesno.infra.data.report.entity;

import com.alinesno.infra.common.facade.mapper.entity.InfraBaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 上报表单模型
 */
@TableName("report_model")
public class ReportModelEntity extends InfraBaseEntity {

    @TableField("business_name")
    private String businessName; // 业务名称，字段用途的描述

    @TableField("name")
    private String name; // 字段名称

    @TableField("field")
    private String field; // 字段标识

    @TableField("min_length")
    private int minLength; // 最小长度，字段值的最小长度限制

    @TableField("max_length")
    private int maxLength; // 最大长度，字段值的最大长度限制

    @TableField("has_null")
    private String hasNull; // 是否允许为空，表示字段值是否可以为空

    @TableField("key")
    private String key; // 是否为主字段，表示是否为主要字段，不能为空，不存在则自动填充

    @TableField("type")
    private String type; // 字段类型，表示字段的数据类型

    @TableField("value")
    private String value; // 字段值，表示字段的当前值

    // 省略 getter 和 setter 方法
}
