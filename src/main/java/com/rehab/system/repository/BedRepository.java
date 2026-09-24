package com.rehab.system.repository;

import com.rehab.system.entity.Bed;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BedRepository extends JpaRepository<Bed, Long> {

    long countByStatus(String status);
}
