package com.rehab.system.repository;

import com.rehab.system.entity.ScaleItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScaleItemRepository extends JpaRepository<ScaleItem, Long> {

    List<ScaleItem> findByScale_IdOrderBySortAsc(Long scaleId);

    void deleteByScale_Id(Long scaleId);
}
