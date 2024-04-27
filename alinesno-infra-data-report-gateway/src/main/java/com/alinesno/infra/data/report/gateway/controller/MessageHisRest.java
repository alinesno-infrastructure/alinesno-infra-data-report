package com.alinesno.infra.data.report.gateway.controller;


import com.alinesno.infra.common.core.constants.SpringInstanceScope;
import com.alinesno.infra.common.facade.pageable.DatatablesPageBean;
import com.alinesno.infra.common.facade.pageable.TableDataInfo;
import com.alinesno.infra.common.web.adapter.plugins.TranslateCode;
import com.alinesno.infra.common.web.adapter.rest.BaseController;
import com.alinesno.infra.data.report.entity.MessageHisEntity;
import com.alinesno.infra.data.report.service.IMessageHisService;
import com.alinesno.infra.data.report.util.Constants;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

/**
 * 【保存消息】Rest
 *
 * @author paul
 * @date 2022-05-09 09:33:44
 */
@RestController
@Scope(SpringInstanceScope.PROTOTYPE)
@RequestMapping("/api/infra/data/report/MessageHis")
public class MessageHisRest extends BaseController<MessageHisEntity, IMessageHisService> {

	// 日志记录
	private static final Logger log = LoggerFactory.getLogger(MessageHisRest.class);

	@Autowired
	private IMessageHisService messageHisService;

	@TranslateCode(plugin = "MessageHisPlugin")
//	@DataFilter
	@ResponseBody
	@PostMapping("/datatables")
	public TableDataInfo datatables(HttpServletRequest request, Model model, DatatablesPageBean page) {
		log.debug("page = {}", ToStringBuilder.reflectionToString(page));
		this.setConditions(page);
		return this.toPage(model, this.getFeign(), page);
	}

	@Override
	public IMessageHisService getFeign() {
		return this.messageHisService;
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



}


