package com.alinesno.infra.data.report.entity;

import com.alinesno.infra.common.facade.mapper.entity.InfraBaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 上报数据表
 */
@TableName("report_data")
public class ReportDataEntity extends InfraBaseEntity {

    @TableField("model_id")
    private Long modelId; // 模型ID

    @TableField("file_name")
    private String fileName; // 文件名

    @TableField("data")
    private String data; // 上报数据

    // 省略 getter 和 setter 方法
}
