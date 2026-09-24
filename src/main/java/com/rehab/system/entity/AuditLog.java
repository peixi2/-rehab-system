package com.rehab.system.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "audit_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog extends BaseEntity {

    private Long userId;

    @Column(length = 50)
    private String username;

    @Column(length = 20)
    private String role;

    @Column(length = 50)
    private String module;

    @Column(length = 50)
    private String action;

    @Column(length = 500)
    private String detail;

    @Column(length = 50)
    private String ip;
}
