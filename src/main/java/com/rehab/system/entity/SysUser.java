package com.rehab.system.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sys_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SysUser extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false, length = 20)
    private String role;

    @Column(length = 50)
    private String realName;

    private Long doctorId;

    /** 患者角色账号绑定的病人ID */
    private Long patientId;

    @Column(nullable = false)
    private Boolean enabled = true;
}
