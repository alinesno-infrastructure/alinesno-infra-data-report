package com.alinesno.infra.data.report.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.alinesno.infra.common.core.service.impl.IBaseServiceImpl;
import com.alinesno.infra.common.facade.response.AjaxResult;
import com.alinesno.infra.common.facade.wrapper.RpcWrapper;
import com.alinesno.infra.data.report.entity.MessageEntity;
import com.alinesno.infra.data.report.entity.MessageHisEntity;
import com.alinesno.infra.data.report.mapper.MessageMapper;
import com.alinesno.infra.data.report.service.IMessageHisService;
import com.alinesno.infra.data.report.service.IMessageService;
import com.alinesno.infra.data.report.service.KafkaProducerService;
import com.alinesno.infra.data.report.vo.ResponseBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 【保存消息】Service业务层处理
 *
 * @author paul
 * @date 2022-05-09 09:33:44
 */
@Service
public class MessageServiceImpl extends IBaseServiceImpl<MessageEntity, MessageMapper>
		implements IMessageService {
	// 日志记录
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(MessageServiceImpl.class);


	@Autowired
	private KafkaProducerService kafkaProducerService;

	@Autowired
	private IMessageHisService messageHisService ;

	// 引入系统参数配置
//	@Autowired
//	private AuthorityConfigClient authorityConfigClient;

	public AjaxResult sendToBus(List<MessageEntity> messageList){

		log.debug("符合查询条件的记录数：{}",messageList.size());

		for (MessageEntity messageEntity : messageList) {
			ResponseBean responseBean = kafkaProducerService.sendMessage(messageEntity.getTopice(), messageEntity.getData());
			if ( responseBean.getCode() != 200 ) {
				messageEntity.setRetryCount( messageEntity.getRetryCount() + 1 );
			} else {
				messageEntity.setIfSendBus(1);

			}

		}

		this.saveOrUpdateBatch(messageList) ;

		return  AjaxResult.success("成功发送数据总线!");


	};

	public void migrateSendData(){
		int  storeHours = -168 ;
//		ManagerSettingsEntity retentionHours = authorityConfigClient.getConfigByKey("reportTimeMigration");
//		if ( retentionHours != null ) {
//			try {
//				storeHours = -Integer.parseInt( retentionHours.getConfigValue() );
//
//			} catch (NumberFormatException e) {
//				log.error("参数值:{},无法转换为数字!请联系系统管理员!",retentionHours.getConfigValue() );
//
//			}
//
//		}

		Date date= DateUtil.parse(DateUtil.now());
		DateTime dateTime = DateUtil.offsetHour(date, storeHours);

		RpcWrapper<MessageEntity> messageWrapper = new RpcWrapper<>();
		messageWrapper.le("add_time",dateTime);

		ArrayList<MessageHisEntity> messageHisList = new ArrayList<>() ;
		List<MessageEntity> messagelist = this.findAll(messageWrapper);
		for (MessageEntity tmpMessage : messagelist) {
			MessageHisEntity tmpMessageHisEntity = new MessageHisEntity();
			tmpMessageHisEntity.setModelId( tmpMessage.getModelId() );
			tmpMessageHisEntity.setFileId(  tmpMessage.getFileId() );
			tmpMessageHisEntity.setTopice( tmpMessage.getTopice() );
			tmpMessageHisEntity.setFileIndex( tmpMessage.getFileIndex() );
			tmpMessageHisEntity.setData( tmpMessage.getData() );
			tmpMessageHisEntity.setReportStatus( tmpMessage.getReportStatus() );
			tmpMessageHisEntity.setRetryCount( tmpMessage.getRetryCount() );
			tmpMessageHisEntity.setTimeout( tmpMessage.getTimeout() );
			tmpMessageHisEntity.setErrorMsg( tmpMessage.getErrorMsg() );
			tmpMessageHisEntity.setMessageType( tmpMessage.getMessageType() );
			tmpMessageHisEntity.setIfSendBus( tmpMessage.getIfSendBus() );
			tmpMessageHisEntity.setOperatorId(tmpMessage.getOperatorId());
			tmpMessageHisEntity.setOldId(String.valueOf(tmpMessage.getId()));
			messageHisList.add(tmpMessageHisEntity) ;
		}

		messageHisService.saveOrUpdateBatch(messageHisList);
		this.deleteByWrapper(messageWrapper);

	};







}
