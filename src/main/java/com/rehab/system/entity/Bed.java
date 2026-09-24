package com.rehab.system.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bed")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Bed extends BaseEntity {

    public static final String STATUS_FREE = "空闲";
    public static final String STATUS_OCCUPIED = "占用";

    @Column(nullable = false, unique = true, length = 20)
    private String bedNo;

    @Column(length = 20)
    private String roomNo;

    @Column(nullable = false, length = 10)
    private String status = STATUS_FREE;
}
