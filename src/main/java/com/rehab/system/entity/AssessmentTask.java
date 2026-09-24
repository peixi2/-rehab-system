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
@Table(name = "assessment_task")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentTask extends BaseEntity {

    public static final String STATUS_PENDING = "待执行";
    public static final String STATUS_COMPLETED = "已完成";
    public static final String STATUS_REJECTED = "已驳回";

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private AssessmentItem item;

    /** 开单医师 */
    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    /** 执行治疗师，执行时记录 */
    @ManyToOne
    @JoinColumn(name = "therapist_id")
    private Doctor therapist;

    @Column(nullable = false, length = 10)
    private String status = STATUS_PENDING;

    private LocalDate applyDate;

    private LocalDate executeDate;

    @Column(length = 255)
    private String rejectReason;

    @Column(length = 255)
    private String note;

    /** 执行后生成的评估记录 */
    private Long recordId;
}
