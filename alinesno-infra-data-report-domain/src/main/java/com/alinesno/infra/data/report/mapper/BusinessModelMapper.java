package com.alinesno.infra.data.report.mapper;

import com.alinesno.infra.common.facade.mapper.repository.IBaseMapper;
import com.alinesno.infra.data.report.entity.BusinessModelEntity;
import org.springframework.stereotype.Repository;
import java.util.Map;

/**
 * 【请填写功能名称】Mapper接口
 *
 * @author paul
 * @date 2024年3月10日
 */
@Repository
public interface BusinessModelMapper extends IBaseMapper<BusinessModelEntity> {

    //创建数据库表
    void createTable(String createTableSql);

    //检查数据库表是否存在
    Map<String, String> checkTableExistsWithShow(String tableName);

}
