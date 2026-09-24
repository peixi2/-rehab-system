package com.rehab.system.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AssessmentRecordRequest {

    @NotNull(message = "请选择病人")
    private Long patientId;

    @NotNull(message = "请选择评估项目")
    private Long itemId;

    @NotNull(message = "请选择评估医生")
    private Long doctorId;

    private Integer score;

    private String result;

    private LocalDate assessDate;

    private String detail;
}
