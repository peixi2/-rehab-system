package com.rehab.system.repository;

import com.rehab.system.entity.TreatmentItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TreatmentItemRepository extends JpaRepository<TreatmentItem, Long> {
}
