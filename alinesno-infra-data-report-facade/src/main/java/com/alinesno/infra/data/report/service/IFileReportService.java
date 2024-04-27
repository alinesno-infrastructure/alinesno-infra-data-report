package com.alinesno.infra.data.report.service;

import com.alinesno.infra.common.facade.response.AjaxResult;
import com.alinesno.infra.common.facade.services.IBaseService;
import com.alinesno.infra.data.report.entity.FileReportEntity;
import com.alinesno.infra.data.report.vo.FileReportAbnlStats;
import com.alinesno.infra.data.report.vo.FileReportMsgStats;
import com.alinesno.infra.data.report.vo.FileReportTodayStats;
import com.alinesno.infra.data.report.vo.ResponseBean;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import java.util.List;

/**
 * 【上报文件】Service接口
 */
public interface IFileReportService extends IBaseService<FileReportEntity>{

    IPage<FileReportEntity> recycleFileList(String operatorId, Long currentPage, Long pageCount, String fileName);

    void reductionFileReport(String id);

    void deleteFileReport( String id);

    FileReportTodayStats FileReportTodayStats(String operatorId);

    FileReportMsgStats FileReportMsgStats(String operatorId);

    FileReportAbnlStats FileReportAbnlStats(String operatorId);

    //消息对象为表时，执行相关的SQL语句
    void insertTable(String tableName,String values);

    //消息对象为表时，执行相关的SQL语句
    void executeSql(String sqlContent );

    //遍历minIO中的文件对象，并将文件对象转入checkDelete函数，如文件对象在业务模型、上报文件中不存在，则删除minio中的文件对象
    void timingDeleteFile();

    //删除minIO中的文件对象
    void deleteMinioOject(String fileUrl);

    //根据ID查询回收站中的数据，findByIdS方法过滤了hasDelete= 1的数据
    List<FileReportEntity> findByReportIds( QueryWrapper<FileReportEntity> queryWrapper );

    //检查同一个用户下，上报文件名称是否存在，如已存在，则不允许保存。确同一个用户下上报文件名称唯一
    AjaxResult checkIfExist(FileReportEntity fileReport);

    ResponseBean checkHasMessage(List<String> idList, String operatorId);

    ResponseBean checkHasMessageHis(List<String> idList, String operatorId);

    ResponseBean checkHasFileShare(List<String> idList, String operatorId);

}
