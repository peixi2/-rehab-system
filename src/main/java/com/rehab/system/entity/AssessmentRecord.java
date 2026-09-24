package com.rehab.system.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "assessment_record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentRecord extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private AssessmentItem item;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    private Integer score;

    @Column(length = 255)
    private String result;

    private LocalDate assessDate;

    /** 量表作答明细 JSON（条目选择/数值），用于追溯与复评对比 */
    @Column(columnDefinition = "TEXT")
    private String detail;
}
