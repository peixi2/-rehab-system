package com.rehab.system.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "patient")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Patient extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 10)
    private String gender;

    private Integer age;

    @Column(length = 20)
    private String phone;

    @Column(length = 255)
    private String diagnosis;

    private LocalDate admissionDate;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @OneToOne
    @JoinColumn(name = "bed_id", unique = true)
    private Bed bed;
}
