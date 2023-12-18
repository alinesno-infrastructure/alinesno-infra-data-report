package com.alinesno.infra.data.report.gateway.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ReportModelDTO 参数校验测试")
public class ReportModelDTOTest {

    private final Validator validator;

    public ReportModelDTOTest() {
        ValidatorFactory factory = Validation.byDefaultProvider().configure().buildValidatorFactory(); ;
        this.validator = factory.getValidator();
    }

    @Test
    @DisplayName("测试参数校验")
    public void testReportModelDTOValidation() {
        ReportModelDTO reportModelDTO = new ReportModelDTO();
        reportModelDTO.setBusinessName("");
        reportModelDTO.setName("");
        reportModelDTO.setField("");
        reportModelDTO.setMinLength(null);
        reportModelDTO.setMaxLength(null);
        reportModelDTO.setHasNull("");
        reportModelDTO.setKey("");
        reportModelDTO.setType("");

        Set<ConstraintViolation<ReportModelDTO>> violations = validator.validate(reportModelDTO);

        assertEquals(8, violations.size());
        assertTrue(containsViolation(violations, "业务名称不能为空"));
        assertTrue(containsViolation(violations, "字段名称不能为空"));
        assertTrue(containsViolation(violations, "字段标识不能为空"));
        assertTrue(containsViolation(violations, "最小长度不能为空"));
        assertTrue(containsViolation(violations, "最大长度不能为空"));
        assertTrue(containsViolation(violations, "是否允许为空不能为空"));
        assertTrue(containsViolation(violations, "是否为主字段不能为空"));
        assertTrue(containsViolation(violations, "字段类型不能为空"));
    }

    private boolean containsViolation(Set<ConstraintViolation<ReportModelDTO>> violations, String message) {
        return violations.stream()
                .anyMatch(violation -> message.equals(violation.getMessage()));
    }
}
