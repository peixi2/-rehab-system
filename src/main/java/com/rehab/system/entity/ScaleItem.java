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

@Entity
@Table(name = "scale_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScaleItem extends BaseEntity {

    public static final String TYPE_SINGLE = "SINGLE";
    public static final String TYPE_MULTI = "MULTI";
    public static final String TYPE_NUMBER = "NUMBER";
    public static final String TYPE_TEXT = "TEXT";

    @ManyToOne
    @JoinColumn(name = "scale_id", nullable = false)
    private AssessmentItem scale;

    @Column(nullable = false)
    private Integer itemNo;

    @Column(nullable = false, length = 500)
    private String question;

    @Column(nullable = false, length = 10)
    private String type = TYPE_SINGLE;

    @Column(nullable = false)
    private Integer sort = 0;
}
