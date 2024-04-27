package com.alinesno.infra.data.report.enums;

import com.alinesno.infra.data.report.enums.enums.BaseEnum;
import java.util.Arrays;
import java.util.Objects;

/**
 * 是否已发数据总线枚举类
 *
 * @author liuguobing
 */

public enum messageSendBusEnum implements BaseEnum<Integer> {
	/**
	 * 未发往数据总线
	 */
	unSend(0, "否"),
	/**
	 * 已发往数据总线
	 */
	send(1, "是");

	private Integer code;
	private String desc;

	public static messageSendBusEnum getEnum(Integer code) {
		return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code)).findFirst().orElse(null);
	}

	public static String getEnumDesc(Integer code) {
		messageSendBusEnum e = getEnum(code);
		return e != null ? e.desc : null;
	}

	@Override
	public Integer getCode() {
		return code;
	}

	@Override
	public String getDesc() {
		return desc;
	}

	private messageSendBusEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

}
