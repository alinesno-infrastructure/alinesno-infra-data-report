package com.alinesno.infra.data.report.entity;

import com.alinesno.infra.common.facade.mapper.entity.InfraBaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.ColumnComment;
import com.gitee.sunchenbin.mybatis.actable.annotation.ColumnType;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
import lombok.Data;


/**
 * 功能名： 【数据模型】
 * 数据表：  business_model
 *
 * @author LuoXiaoDong
 * @version 1.0.0
 */
@Data
@TableName("business_model")
public class BusinessModelEntity extends InfraBaseEntity {
	/**
	 * 名称
	 */
	@ColumnType(length = 80)
	@ColumnComment("名称")
	@TableField("model_name")
	private String modelName;

	/**
	 * 简称
	 */
	@ColumnType(length = 40)
	@ColumnComment("简称")
	@TableField("model_short_name")
	private String modelShortName;

	/**
	 * 父模型
	 */
	@ColumnType(length = 64)
	@ColumnComment("父模型")
	@TableField("model_parent_id")
	private String modelParentId;

	/**
	 * 文件id,minIO中的id
	 */
	@ColumnType(length = 64)
	@ColumnComment("文件id,minIO中的id")
	@TableField("storage_file_id")
	private String storageFileId;

	/**
	 * 文件名称
	 */
	@ColumnType(length = 80)
	@ColumnComment("文件名称")
	@TableField("storage_file_name")
	private String storageFileName;

	/**
	 * 文件后缀
	 */
	@ColumnType(length = 15)
	@ColumnComment("文件后缀")
	@TableField("storage_file_extend_name")
	private String storageFileExtendName;

	/**
	 * 文件全名
	 */
	@ColumnType(length = 100)
	@ColumnComment("文件全名")
	@TableField("storage_file_full_name")
	private String storageFileFullName;

	/**
	 * 文件路径
	 */
	@ColumnType(length = 100)
	@ColumnComment("文件路径")
	@TableField("storage_file_path")
	private String storageFilePath;

	/**
	 * 文件大小
	 */
	@ColumnType(MySqlTypeConstant.BIGINT)
	@ColumnComment("文件大小")
	@TableField("storage_file_size")
	private Long storageFileSize;

	/**
	 * 文件url
	 */
	@ColumnType(length = 200)
	@ColumnComment("文件url")
	@TableField("storage_file_url")
	private String storageFileUrl;

	/**
	 * 是否是目录;0-否,1-是
	 */
	@ColumnType(MySqlTypeConstant.BIGINT)
	@ColumnComment("是否是目录;0-否,1-是")
	@TableField("if_dir")
	private Long ifDir;

	/**
	 * 消息主题
	 */
	@ColumnType(length = 64)
	@ColumnComment("消息主题")
	@TableField("kafka_topice")
	private String kafkaTopice;

	/**
	 * 列数，用于判断上传文件列数是否一致
	 */
	@ColumnType(MySqlTypeConstant.BIGINT)
	@ColumnComment("列数，用于判断上传文件列数是否一致")
	@TableField("column_num")
	private Long columnNum;

	/**
	 * 列中文名列表，用于判断上传文件列名
	 */
	@ColumnType(length = 4000)
	@ColumnComment("列中文名列表，用于判断上传文件列名")
	@TableField("column_cn_name")
	private String columnCnName;

	/**
	 * 列英文名列表，用于判断上传文件列名
	 */
	@ColumnType(length = 4000)
	@ColumnComment("列英文名列表，用于判断上传文件列名")
	@TableField("column_name")
	private String columnName;

	/**
	 * 备注
	 */
	@ColumnType(length = 4000)
	@ColumnComment("备注")
	@TableField("remark")
	private String remark;

	/**
	 * 消息类型
	 */
	@ColumnType(MySqlTypeConstant.BIGINT)
	@ColumnComment("消息类型")
	@TableField("message_type")
	private Integer messageType = 0 ;

}
