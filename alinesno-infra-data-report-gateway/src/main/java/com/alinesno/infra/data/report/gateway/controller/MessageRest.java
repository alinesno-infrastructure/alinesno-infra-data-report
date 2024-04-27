package com.alinesno.infra.data.report.gateway.controller;

import com.alinesno.infra.common.core.constants.SpringInstanceScope;
import com.alinesno.infra.common.facade.pageable.DatatablesPageBean;
import com.alinesno.infra.common.facade.pageable.TableDataInfo;
import com.alinesno.infra.common.facade.response.AjaxResult;
import com.alinesno.infra.common.web.adapter.plugins.TranslateCode;
import com.alinesno.infra.common.web.adapter.rest.BaseController;
import com.alinesno.infra.data.report.entity.MessageEntity;
import com.alinesno.infra.data.report.service.IMessageService;
import com.alinesno.infra.data.report.util.Constants;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * 【保存消息】Rest
 *
 * @author paul
 * @date 2022-05-09 09:33:44
 */
@RestController
@Scope(SpringInstanceScope.PROTOTYPE)
@RequestMapping("/api/infra/data/report/Message")
public class MessageRest extends BaseController<MessageEntity, IMessageService> {

	// 日志记录
	private static final Logger log = LoggerFactory.getLogger(MessageRest.class);

	@Autowired
	private IMessageService messageService;

	@TranslateCode(plugin = "MessagePlugin")
//	@DataFilter
	@ResponseBody
	@PostMapping("/datatables")
	public TableDataInfo datatables(HttpServletRequest request, Model model, DatatablesPageBean page) {
		log.debug("page = {}", ToStringBuilder.reflectionToString(page));
		this.setConditions(page);
		return this.toPage(model, this.getFeign(), page);
	}

	@Override
	public IMessageService getFeign() {
		return this.messageService;
	}

	private void setConditions(DatatablesPageBean page) {
		Map<String, Object> condition = page.getCondition();
		if (MapUtils.isNotEmpty(condition)) {
			String startDate = condition.get("addTime|geTime") == null ? null
					: (String) condition.get("addTime|geTime");
			String endDate = condition.get("addTime|leTime") == null ? null : (String) condition.get("addTime|leTime");

			if (StringUtils.isNotBlank(startDate)) {
				if (startDate.length() == Constants.DATE_TIME_LENGTH) {
					/* yyyy-MM-dd HH:mm:ss */
//					String[] arr = startDate.split(" ");
//					startDate = arr[0] + " 00:00:00";
				} else if (startDate.length() == Constants.DATE_LENGTH) {
					/* yyyy-MM-dd */
					startDate = startDate.trim() + " 00:00:00";
				}
				condition.put("addTime|geTime", startDate);
			}
			if (StringUtils.isNotBlank(endDate)) {
				if (endDate.length() == Constants.DATE_TIME_LENGTH) {
					/* yyyy-MM-dd HH:mm:ss */
//					String[] arr = endDate.split(" ");
//					endDate = arr[0] + " 23:59:59";
				} else if (endDate.length() == Constants.DATE_LENGTH) {
					/* yyyy-MM-dd */
					endDate = endDate.trim() + " 23:59:59";
				}
				condition.put("addTime|leTime", endDate);
			}
			condition.put("has_delete|", 0);
			page.setCondition(condition);
		}
	}

	@ResponseBody
	@GetMapping("/sendToBus")
	private AjaxResult sendToBus(String messageID, String fileID, String ifOne) {

		log.debug("查询条件,messageID={},fileID={},ifOne={}", messageID, fileID, ifOne);

		QueryWrapper<MessageEntity> messageWrapper = new QueryWrapper<>();
		messageWrapper.eq("if_send_bus",0);
		log.debug("查询条件,if_send_bus={}",0);

		// ifOne 为 1 时，将当前messageID记录发送数据总线
		if ( ifOne != null && ifOne.equals("1") ) {
			messageWrapper.eq("id",messageID);
			log.debug("查询条件,id={}",messageID);
		} //ifOne 为 0 时，将当前messageID所在文件未发送数据总线的记录全部发送数据总线
		else if ( ifOne != null && ifOne.equals("0") ) {
			messageWrapper.eq("file_id",fileID);
			messageWrapper.eq("if_send_bus",0);
			log.debug("查询条件,file_id={},if_send_bus=0",fileID);

		}

		log.debug("查询条件{}",messageWrapper.toString());

		List<MessageEntity> messageList = this.getFeign().list(messageWrapper) ;

		return  this.getFeign().sendToBus(messageList);



	}


}


