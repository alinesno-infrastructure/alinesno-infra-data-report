package com.alinesno.infra.data.report.service;

import com.alinesno.infra.common.facade.response.AjaxResult;
import com.alinesno.infra.common.facade.services.IBaseService;
import com.alinesno.infra.data.report.entity.MessageEntity;

import java.util.List;


/**
 * 【保存消息】Service接口
 *
 * @author paul
 * @date 2022-05-09 09:33:44
 */
public interface IMessageService extends IBaseService<MessageEntity> {


    AjaxResult sendToBus(List<MessageEntity> messageList);

    void migrateSendData();
}
