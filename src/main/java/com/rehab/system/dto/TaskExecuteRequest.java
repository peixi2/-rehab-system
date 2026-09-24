package com.rehab.system.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TaskExecuteRequest {

    /** 执行治疗师 */
    private Long therapistId;

    private Integer score;

    private String result;

    /** 量表作答明细 JSON */
    private String detail;

    private LocalDate assessDate;
}
