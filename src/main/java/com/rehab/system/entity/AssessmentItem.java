package com.rehab.system.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "assessment_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentItem extends BaseEntity {

    public static final String STATUS_ENABLED = "启用";
    public static final String STATUS_DISABLED = "停用";

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 50)
    private String code;

    @Column(length = 20)
    private String version;

    @Column(nullable = false, length = 10)
    private String status = STATUS_ENABLED;

    @Column(length = 255)
    private String description;

    @Column(length = 1000)
    private String instructions;

    /** 功能分级规则 JSON：[{"min":0,"max":44,"grade":"重度功能障碍"},...] */
    @Column(length = 2000)
    private String gradingRules;
}
