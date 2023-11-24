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
@TableName("file_report")
public class FileReportEntity extends InfraBaseEntity {
	/**
	 * 文件名称
	 */
	@ColumnType(length = 80)
	@ColumnComment("文件名称")
	@TableField("file_name")
	private String fileName;

	/**
	 * 文件后缀
	 */
	@ColumnType(length = 15)
	@ColumnComment("文件后缀")
	@TableField("file_extend_name")
	private String fileExtendName;

	/**
	 * 文件全名
	 */
	@ColumnType(length = 100)
	@ColumnComment("文件全名")
	@TableField("file_full_name")
	private String fileFullName;

	/**
	 * 文件大小
	 */
	@ColumnType(MySqlTypeConstant.BIGINT)
	@ColumnComment("文件大小")
	@TableField("file_size")
	private Long fileSize;

	/**
	 * 是否是目录;0-否,1-是
	 */
	@ColumnType(MySqlTypeConstant.BIGINT)
	@ColumnComment("是否是目录;0-否,1-是")
	@TableField("if_dir")
	private Long ifDir;

	/**
	 * 存储类型，默认是0，minIO
	 */
	@ColumnType(MySqlTypeConstant.BIGINT)
	@ColumnComment("存储类型，默认是0，minIO")
	@TableField("storage_type")
	private Long storageType;

	/**
	 * 文件id,minIO中的id
	 */
	@ColumnType(length = 64)
	@ColumnComment("文件id,minIO中的id")
	@TableField("storage_file_id")
	private String storageFileId;

	/**
	 * 文件路径
	 */
	@ColumnType(length = 100)
	@ColumnComment("文件路径")
	@TableField("storage_file_path")
	private String storageFilePath;

	/**
	 * 文件url
	 */
	@ColumnType(length = 200)
	@ColumnComment("文件url")
	@TableField("storage_file_url")
	private String storageFileUrl;

	/**
	 * 模型id
	 */
	@ColumnType(length = 64)
	@ColumnComment("模型id")
	@TableField("model_id")
	private String modelId;

	/**
	 * 校验是否通过;0-否,1-是
	 */
	@ColumnType(MySqlTypeConstant.BIGINT)
	@ColumnComment("校验是否通过;0-否,1-是")
	@TableField("if_check")
	private Long ifCheck;

	/**
	 * 校验文件id
	 */
	@ColumnType(length = 64)
	@ColumnComment("校验文件id")
	@TableField("check_file_id")
	private String checkFileId;

	/**
	 * 校验文件名称
	 */
	@ColumnType(length = 80)
	@ColumnComment("校验文件名称")
	@TableField("check_file_name")
	private String checkFileName;

	/**
	 * 校验文件后缀
	 */
	@ColumnType(length = 15)
	@ColumnComment("校验文件后缀")
	@TableField("check_file_extend_name")
	private String checkFileExtendName;

	/**
	 * 校验文件路径
	 */
	@ColumnType(length = 100)
	@ColumnComment("校验文件路径")
	@TableField("check_file_path")
	private String checkFilePath;

	/**
	 * 校验文件url
	 */
	@ColumnType(length = 200)
	@ColumnComment("校验文件url")
	@TableField("check_file_url")
	private String checkFileUrl;

	/**
	 * 上报状态;0-否,1-是
	 */
	@ColumnType(MySqlTypeConstant.BIGINT)
	@ColumnComment("上报状态;0-否,1-是")
	@TableField("report_status")
	private Long reportStatus;

	/**
	 * 上报数据行数
	 */
	@ColumnType(MySqlTypeConstant.BIGINT)
	@ColumnComment("上报数据行数")
	@TableField("report_row")
	private Long reportRow;

	/**
	 * 上报时间
	 */
	@ColumnType(value = MySqlTypeConstant.DATETIME, length = 18)
	@ColumnComment("上报时间")
	@TableField("report_time")
	private Date reportTime;

	/**
	 * 备注
	 */
	@ColumnType(length = 4000)
	@ColumnComment("备注")
	@TableField("remark")
	private String remark;

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
