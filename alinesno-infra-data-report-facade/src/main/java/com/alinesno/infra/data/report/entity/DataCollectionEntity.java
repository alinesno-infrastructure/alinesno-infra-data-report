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
@TableName("data_collection")
public class DataCollectionEntity extends InfraBaseEntity {
	/**
	 * 数据源URL
	 */
	@ColumnType(length = 200)
	@ColumnComment("数据源URL")
	@TableField("data_source_url")
	private String dataSourceUrl;

	/**
	 * 采集用的shell脚本
	 */
	@ColumnType(MySqlTypeConstant.TEXT)
	@ColumnComment("采集用的shell脚本")
	@TableField("shell_script")
	private String shellScript;

	/**
	 * 上报系统提供的API接口
	 */
	@ColumnType(length = 200)
	@ColumnComment("上报系统提供的API接口")
	@TableField("api_interface")
	private String apiInterface;

	/**
	 * 采集时间
	 */
	@ColumnType(value = MySqlTypeConstant.DATETIME, length = 18)
	@ColumnComment("采集时间")
	@TableField("collection_time")
	private Date collectionTime;

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
