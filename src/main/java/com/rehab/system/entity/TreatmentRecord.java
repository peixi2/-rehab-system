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
@Table(name = "treatment_record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TreatmentRecord extends BaseEntity {

    public static final String STATUS_ONGOING = "进行中";
    public static final String STATUS_FINISHED = "已完成";

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private TreatmentItem item;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(nullable = false, length = 10)
    private String status = STATUS_ONGOING;

    private LocalDate startDate;

    private LocalDate endDate;

    @Column(length = 255)
    private String note;
}
