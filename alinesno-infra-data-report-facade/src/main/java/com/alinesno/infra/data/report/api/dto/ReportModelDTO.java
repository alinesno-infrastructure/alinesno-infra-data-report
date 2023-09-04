package com.alinesno.infra.data.report.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ReportModelDTO {

    @NotBlank(message = "业务名称不能为空")
    private String businessName;

    @NotBlank(message = "字段名称不能为空")
    private String name;

    @NotBlank(message = "字段标识不能为空")
    private String field;

    @NotNull(message = "最小长度不能为空")
    private Integer minLength;

    @NotNull(message = "最大长度不能为空")
    private Integer maxLength;

    @NotBlank(message = "是否允许为空不能为空")
    private String hasNull;

    @NotBlank(message = "是否为主字段不能为空")
    private String key;

    @NotBlank(message = "字段类型不能为空")
    private String type;

    private String value;

    // 省略 getter 和 setter 方法

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public Integer getMinLength() {
        return minLength;
    }

    public void setMinLength(Integer minLength) {
        this.minLength = minLength;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    public String getHasNull() {
        return hasNull;
    }

    public void setHasNull(String hasNull) {
        this.hasNull = hasNull;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
