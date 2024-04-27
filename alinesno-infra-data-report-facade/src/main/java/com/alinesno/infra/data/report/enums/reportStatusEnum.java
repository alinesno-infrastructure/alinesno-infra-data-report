package com.alinesno.infra.data.report.enums;

import com.alinesno.infra.data.report.enums.enums.BaseEnum;
import java.util.Arrays;
import java.util.Objects;

/**
 * 上报状态枚举类
 *
 * @author liuguobing
 */

public enum reportStatusEnum implements BaseEnum<Integer> {
	/**
	 * 待上报
	 */
	justUploaded(0, "待上报"),
	/**
	 * 上报成功
	 */
	success(1, "上报成功"),	/**
	 * 上报异常
	 */
	anomaly(2, "上报异常"),	/**
	 * 发送中
	 */
	sending(3, "发送中");

	private Integer code;
	private String desc;

	public static reportStatusEnum getEnum(Integer code) {
		return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code)).findFirst().orElse(null);
	}

	public static String getEnumDesc(Integer code) {
		reportStatusEnum e = getEnum(code);
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

	private reportStatusEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

}
