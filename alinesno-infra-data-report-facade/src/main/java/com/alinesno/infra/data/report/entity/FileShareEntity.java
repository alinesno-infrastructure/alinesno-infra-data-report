package com.alinesno.infra.data.report.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.alibaba.fastjson.annotation.JSONField;
import com.alinesno.infra.common.facade.mapper.entity.InfraBaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.ColumnComment;
import com.gitee.sunchenbin.mybatis.actable.annotation.ColumnType;
import lombok.Data;
import java.util.Date;

/**
 * 功能名： 【数据文件分享】
 * 数据表：  file_share
 * 表备注：
 * @author paul
 * @date 2024-03-24 17:55:47
 */
@Data
@TableName("file_share")
public class FileShareEntity extends InfraBaseEntity {
    private static final long serialVersionUID = 1L;

    /**
    * 失效时间
    */
    @ColumnComment("失效时间")
    @Excel(name="失效时间",exportFormat = "yyyy-MM-dd")
    @ColumnType(length = 0)
    @TableField("end_time")
    @JSONField(format = "yyyy-MM-dd")
    private Date endTime;

    /**
     * 是否需要验证码
     */
    @ColumnComment("是否需要验证码")
    @Excel(name="是否需要验证码")
    @ColumnType(length = 1)
    @TableField("if_code")
    private int ifCode;

    /**
    * 提取码
    */
    @ColumnComment("提取码")
    @Excel(name="提取码")
    @ColumnType(length = 10)
    @TableField("extraction_code")
    private String extractionCode;

    /**
    * 分享批次号
    */
    @ColumnComment("分享批次号")
    @Excel(name="分享批次号")
    @ColumnType(length = 40)
    @TableField("share_batch_num")
    private String shareBatchNum;

    /**
    * 分享状态(0正常,1已失效,2已撤销)
    */
    @ColumnComment("分享状态(0正常,1已失效,2已撤销)")
    @Excel(name="分享状态(0正常,1已失效,2已撤销)")
    @ColumnType(length = 0)
    @TableField("share_status")
    private Long shareStatus;

    /**
    * 分享类型(0公共,1私密,2好友)
    */
    @ColumnComment("分享类型(0公共,1私密,2好友)")
    @Excel(name="分享类型(0公共,1私密,2好友)")
    @ColumnType(length = 0)
    @TableField("share_type")
    private Long shareType;

    /**
    * 分享用户id
    */
    @ColumnComment("分享用户id")
    @Excel(name="分享用户id")
    @ColumnType(length = 64)
    @TableField("share_user_id")
    private String shareUserId;

    /**
    * 分享文件类型(0业务模型,1上报文件)
    */
    @ColumnComment("分享文件类型(0业务模型,1上报文件)")
    @Excel(name="分享文件类型(0业务模型,1上报文件)")
    @ColumnType(length = 0)
    @TableField("share_file_type")
    private Long shareFileType;

    /**
    * 分享文件ID
    */
    @ColumnComment("分享文件ID")
    @Excel(name="分享文件ID")
    @ColumnType(length = 64)
    @TableField("share_file_id")
    private String shareFileId;

    /**
    * 文件名称
    */
    @ColumnComment("文件名称")
    @Excel(name="文件名称")
    @ColumnType(length = 80)
    @TableField("share_file_name")
    private String shareFileName;

    /**
    * 文件后缀
    */
    @ColumnComment("文件后缀")
    @Excel(name="文件后缀")
    @ColumnType(length = 15)
    @TableField("share_file_extend_name")
    private String shareFileExtendName;

    /**
    * 文件全名
    */
    @ColumnComment("文件全名")
    @Excel(name="文件全名")
    @ColumnType(length = 100)
    @TableField("share_file_full_name")
    private String shareFileFullName;

    /**
    * 文件大小
    */
    @ColumnComment("文件大小")
    @Excel(name="文件大小")
    @ColumnType(length = 0)
    @TableField("share_file_size")
    private Long shareFileSize;

    /**
    * 文件id,minIO中的id
    */
    @ColumnComment("文件id,minIO中的id")
    @Excel(name="文件id,minIO中的id")
    @ColumnType(length = 64)
    @TableField("storage_file_id")
    private String storageFileId;

    /**
    * 文件路径
    */
    @ColumnComment("文件路径")
    @Excel(name="文件路径")
    @ColumnType(length = 100)
    @TableField("storage_file_path")
    private String storageFilePath;

}
