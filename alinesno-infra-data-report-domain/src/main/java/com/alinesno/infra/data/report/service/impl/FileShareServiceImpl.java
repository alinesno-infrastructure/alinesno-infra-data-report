package com.alinesno.infra.data.report.service.impl;

import com.alinesno.infra.common.core.service.impl.IBaseServiceImpl;
import com.alinesno.infra.data.report.entity.FileShareEntity;
import com.alinesno.infra.data.report.mapper.FileShareMapper;
import com.alinesno.infra.data.report.service.IFileShareService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


/**
 * 【请填写功能名称】Service业务层处理
 *
 * @author paul
 * @date 2022-11-28 10:28:04
 */
@Service
public class FileShareServiceImpl extends IBaseServiceImpl< FileShareEntity, FileShareMapper> implements IFileShareService {
    //日志记录
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(FileShareServiceImpl.class);
}
