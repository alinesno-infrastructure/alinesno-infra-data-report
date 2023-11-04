package com.alinesno.infra.data.report.entity;

import com.alinesno.infra.common.facade.mapper.entity.InfraBaseEntity;
import com.gitee.sunchenbin.mybatis.actable.annotation.ColumnComment;
import com.gitee.sunchenbin.mybatis.actable.annotation.ColumnType;
import lombok.Data;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 上报数据表
 */
@TableName("report_data")
@Data
public class ReportDataEntity extends InfraBaseEntity {

    @TableField("model_id")
	@ColumnType(length=255)
	@ColumnComment("模型ID")
    private Long modelId; // 模型ID

    @TableField("file_name")
	@ColumnType(length=255)
	@ColumnComment("文件名")
    private String fileName; // 文件名

    @TableField("data")
	@ColumnType(length=255)
	@ColumnComment("数据")
    private String data; // 上报数据
}
