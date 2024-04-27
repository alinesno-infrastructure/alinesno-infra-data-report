package com.alinesno.infra.data.report.gateway.controller;


import com.alinesno.infra.common.core.constants.SpringInstanceScope;
import com.alinesno.infra.common.facade.pageable.DatatablesPageBean;
import com.alinesno.infra.common.facade.pageable.TableDataInfo;
import com.alinesno.infra.common.facade.response.AjaxResult;
import com.alinesno.infra.common.web.adapter.plugins.TranslateCode;
import com.alinesno.infra.common.web.adapter.rest.BaseController;
import com.alinesno.infra.data.report.entity.MessageFailEntity;
import com.alinesno.infra.data.report.service.IMessageFailService;
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
import java.util.Map;

/**
 * 【请填写功能名称】Rest
 *
 * @author lguob ${authorEmail}
 * @date 2023-04-26 14:36:27
 */
@RestController
@Scope(SpringInstanceScope.PROTOTYPE)
@RequestMapping("/api/infra/data/report/MqMessageFail")
public class MqMessageFailRest extends BaseController<MessageFailEntity, IMessageFailService> {

    //日志记录
	private static final Logger log = LoggerFactory.getLogger(MqMessageFailRest.class);

	@Autowired
	private IMessageFailService messageFailService;

	@TranslateCode(plugin = "MessageFailPlugin")
//	@DataFilter
	@ResponseBody
	@PostMapping("/datatables")
	public TableDataInfo datatables(HttpServletRequest request, Model model, DatatablesPageBean page) {
		log.debug("page = {}", ToStringBuilder.reflectionToString(page));
		this.setConditions(page);
		return this.toPage(model, this.getFeign(), page);
	}

	@Override
	public IMessageFailService getFeign() {
		return this.messageFailService;
	}

	private void setConditions(DatatablesPageBean page) {
		Map<String, Object> condition = page.getCondition();
		if (MapUtils.isNotEmpty(condition)) {
			String startDate = condition.get("addTime|geTime") == null ? null
					: (String) condition.get("addTime|geTime");
			String endDate = condition.get("addTime|leTime") == null ? null : (String) condition.get("addTime|leTime");

			if (StringUtils.isNotBlank(startDate)) {
				if ( startDate.length() == 19 ) {
					/* yyyy-MM-dd HH:mm:ss */
//					String[] arr = startDate.split(" ");
//					startDate = arr[0] + " 00:00:00";
				} else if ( startDate.length() == 10 ) {
					/* yyyy-MM-dd */
					startDate = startDate.trim() + " 00:00:00";
				}
			}
			if (StringUtils.isNotBlank(endDate)) {
				if (endDate.length() == 19 ) {
					/* yyyy-MM-dd HH:mm:ss */
//					String[] arr = endDate.split(" ");
//					endDate = arr[0] + " 23:59:59";
				} else if (endDate.length() == 10 ) {
					/* yyyy-MM-dd */
					endDate = endDate.trim() + " 23:59:59";
				}
			}
			condition.put("addTime|leTime", endDate);
			condition.put("addTime|geTime", startDate);
			condition.put("has_delete|", 0);
			page.setCondition(condition);
		}
	}

	/**
	 * 重发异常消息
	 * @param messageFail 异常消息
	 */
	@ResponseBody
	@PostMapping ("/retrySend")
	public AjaxResult retrySend(HttpServletRequest request, @RequestBody MessageFailEntity messageFail) {
		log.debug("重发信息：{}",messageFail.toString());
		boolean sendResult = messageFailService.retrySend(messageFail);
		if ( sendResult ) {
			return AjaxResult.success();
		} else {
			return AjaxResult.error("重发失败!");
		}

	}
}
