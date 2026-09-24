package com.rehab.system.repository;

import com.rehab.system.entity.ScaleOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ScaleOptionRepository extends JpaRepository<ScaleOption, Long> {

    List<ScaleOption> findByItem_IdInOrderBySortAsc(Collection<Long> itemIds);

    void deleteByItem_IdIn(Collection<Long> itemIds);
}
