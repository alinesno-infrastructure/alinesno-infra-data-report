package com.alinesno.infra.data.report.service.impl;

import com.alinesno.infra.common.core.service.impl.IBaseServiceImpl;
import com.alinesno.infra.common.facade.response.ResultCodeEnum;
import com.alinesno.infra.data.report.dto.ReportInfoDTO;
import com.alinesno.infra.data.report.entity.MessageFailEntity;
import com.alinesno.infra.data.report.mapper.MessageFailMapper;
import com.alinesno.infra.data.report.service.IMessageFailService;
import com.alinesno.infra.data.report.service.KafkaProducerService;
import com.alinesno.infra.data.report.vo.ResponseBean;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 【请填写功能名称】Service业务层处理
 *
 * @author lguob ${authorEmail}
 * @date 2023-04-26 15:33:44
 */
@Service
public class MessageFailServiceImpl extends IBaseServiceImpl<MessageFailEntity, MessageFailMapper>
		implements IMessageFailService {

	// 日志记录
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(MessageFailServiceImpl.class);


	@Autowired
	protected KafkaProducerService kafkaProducer;

	/**
	 * 重发异常消息
	 * @param messageFail      异常消息id
	 */
	public boolean retrySend(MessageFailEntity messageFail){


		boolean ifSuccess = false ;

        // 发送到指定的主题
		ResponseBean result = kafkaProducer.sendMessage(messageFail.getTopic(),  messageFail.getData());

		if (result.getCode() == 200 ) {
			ifSuccess = true;

		}

		MessageFailEntity mqMessageFail = this.findById(messageFail.getId()) ;
		if ( mqMessageFail != null ) {
			// 如果发送到kafka异常，更新历史异常表
			if ( ifSuccess ) {
				mqMessageFail.setStatus("5") ;
			}else {
				mqMessageFail.setErrorMsg(result.getMessage());
			}
			mqMessageFail.setRetryCount( mqMessageFail.getRetryCount() + 1 ) ;
			this.saveOrUpdate(mqMessageFail) ;

		}

        return ifSuccess ;

	}

	public void addMessageFail(ReportInfoDTO reportInfo, String msg, String errorMsg ) {
		MessageFailEntity messageFail = new MessageFailEntity();
		messageFail.setFileReportId(reportInfo.getId());
		messageFail.setModelId(reportInfo.getModelId());
		messageFail.setTopic(reportInfo.getKafkaTopic());
		messageFail.setData(msg);
		messageFail.setStatus("6");
		messageFail.setErrorMsg(errorMsg);
		messageFail.setOperatorId(reportInfo.getOperatorId());
		this.saveOrUpdate(messageFail);
	}

	//检查是否被历史异常引用，如有记录，则不允许删除
	@Override
	public ResponseBean checkHasMessageFail(List<String> idList, String operatorId){
		ResponseBean result = new ResponseBean() ;
		StringBuffer msg = new StringBuffer();
		//检查是否存在数据管理数据
		QueryWrapper<MessageFailEntity> messageFailWrapper = new QueryWrapper<>();
		messageFailWrapper.in("file_report_id",idList);
		messageFailWrapper.eq("operator_id", operatorId);
		List<MessageFailEntity> messageFailList = this.list(messageFailWrapper);
		if ( messageFailList != null && messageFailList.size() > 0 )	{
			Set<String> fileReportSet = new HashSet<String>() ;

			for ( MessageFailEntity  messageFailEntity : messageFailList ) {
				fileReportSet.add( messageFailEntity.getFileReportId() ) ;
				if ( fileReportSet.size() == 3 ){
					break;
				}
			}
			result.setCode(ResultCodeEnum.FAIL);
			result.setData(fileReportSet);

		} else {
			result.setCode(ResultCodeEnum.SUCCESS);

		}
		return result ;

	}

}
