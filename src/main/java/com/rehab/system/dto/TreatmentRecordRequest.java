package com.rehab.system.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TreatmentRecordRequest {

    @NotNull(message = "请选择病人")
    private Long patientId;

    @NotNull(message = "请选择治疗项目")
    private Long itemId;

    @NotNull(message = "请选择治疗医生")
    private Long doctorId;

    private String status;

    private LocalDate startDate;

    private LocalDate endDate;

    private String note;
}
