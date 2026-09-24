package com.rehab.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientRequest {

    @NotBlank(message = "姓名不能为空")
    private String name;

    private String gender;

    private Integer age;

    private String phone;

    private String diagnosis;

    private LocalDate admissionDate;

    private Long doctorId;

    private Long bedId;
}
