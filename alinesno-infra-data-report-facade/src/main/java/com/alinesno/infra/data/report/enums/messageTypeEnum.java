package com.alinesno.infra.data.report.enums;

import com.alinesno.infra.data.report.enums.enums.BaseEnum;
import java.util.Arrays;
import java.util.Objects;

/**
 * 目标库枚举类
 *
 * @author liuguobing
 */

public enum messageTypeEnum implements BaseEnum<Integer> {
	/**
	 * kafka
	 */
	kafka(0, "数据总线"),
	/**
	 * mysql
	 */
	mysql(1, "数据上报");

	private Integer code;
	private String desc;

	public static messageTypeEnum getEnum(Integer code) {
		return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code)).findFirst().orElse(null);
	}

	public static String getEnumDesc(Integer code) {
		messageTypeEnum e = getEnum(code);
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

	private messageTypeEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

}
