package com.rehab.system.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AssessmentTaskRequest {

    @NotNull(message = "请选择病人")
    private Long patientId;

    @NotNull(message = "请选择量表")
    private Long itemId;

    @NotNull(message = "请选择开单医师")
    private Long doctorId;

    private String note;

    private LocalDate applyDate;
}
