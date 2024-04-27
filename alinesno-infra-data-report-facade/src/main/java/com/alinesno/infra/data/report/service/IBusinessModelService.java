package com.alinesno.infra.data.report.service;

import com.alinesno.infra.common.facade.response.AjaxResult;
import com.alinesno.infra.common.facade.services.IBaseService;
import com.alinesno.infra.data.report.dto.ModelDTO;
import com.alinesno.infra.data.report.entity.BusinessModelEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 【数据模型】Service接口
 *
 */
public interface IBusinessModelService extends IBaseService<BusinessModelEntity>{

    //创建数据库表
    void createTable(String createTableSql);

    //检查数据库表是否存在
    Map<String, String> checkTableExistsWithShow(String tableName);

    //检查同一个用户下，业务模型名称是否存在，如已存在，则不允许保存。确同一个用户下业务模型名称唯一
    AjaxResult checkIfExist(BusinessModelEntity businessMode);

    /**
     * 导入参数
     *  @param file excel文件
     * @return
     */
    AjaxResult importModel(ModelDTO modelDTO, MultipartFile file) ;

}
