package com.alinesno.infra.data.report.mapper;


import com.alinesno.infra.common.facade.mapper.repository.IBaseMapper;
import com.alinesno.infra.data.report.entity.FileReportEntity;
import com.alinesno.infra.data.report.vo.FileReportAbnlStats;
import com.alinesno.infra.data.report.vo.FileReportMsgStats;
import com.alinesno.infra.data.report.vo.FileReportTodayStats;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 【请填写功能名称】Mapper接口
 *
 * @author paul
 * @date 2024年3月10日
 */
@Repository
public interface FileReportMapper extends IBaseMapper<FileReportEntity> {
    IPage<FileReportEntity> recycleFileList(Page<?> page, String operatorId, Long currentPage, Long pageCount, String fileName);

    void reductionFileReport(String id);

    void deleteFileReport(String id);

    FileReportTodayStats FileReportTodayStats(String operatorId);

    FileReportMsgStats FileReportMsgStats(String operatorId);

    FileReportAbnlStats FileReportAbnlStats(String operatorId);

    //插入数据
    void insertTable(String tableName,String values);

    void executeSql(String sqlContent );

    List<FileReportEntity> selectReportIds(@Param(Constants.WRAPPER) QueryWrapper<FileReportEntity> ew);
}
