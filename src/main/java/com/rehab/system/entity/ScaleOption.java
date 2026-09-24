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
@Table(name = "scale_option")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScaleOption extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private ScaleItem item;

    @Column(nullable = false, length = 300)
    private String label;

    @Column(nullable = false)
    private Integer score = 0;

    @Column(nullable = false)
    private Integer sort = 0;
}
