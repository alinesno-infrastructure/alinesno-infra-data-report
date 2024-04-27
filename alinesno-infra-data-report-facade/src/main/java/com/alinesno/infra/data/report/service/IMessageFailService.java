package com.alinesno.infra.data.report.service;

import com.alinesno.infra.common.facade.services.IBaseService;
import com.alinesno.infra.data.report.dto.ReportInfoDTO;
import com.alinesno.infra.data.report.entity.MessageFailEntity;
import com.alinesno.infra.data.report.vo.ResponseBean;
import java.util.List;

/**
 * 【发送kafka失败时，保存消息数据表】Service接口
 *
 * @author lguob ${authorEmail}
 * @date 2023-04-26 15:33:44
 */
public interface IMessageFailService extends IBaseService<MessageFailEntity> {

    boolean retrySend(MessageFailEntity messageFail);

    void addMessageFail(ReportInfoDTO reportInfo, String msg, String errorMsg ) ;

    ResponseBean checkHasMessageFail(List<String> idList, String operatorId);

}
