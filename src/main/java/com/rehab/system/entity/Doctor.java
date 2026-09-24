package com.rehab.system.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "doctor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Doctor extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 10)
    private String gender;

    @Column(length = 50)
    private String title;

    @Column(length = 50)
    private String department;

    @Column(length = 20)
    private String phone;
}
