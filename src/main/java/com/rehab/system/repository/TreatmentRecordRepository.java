package com.rehab.system.repository;

import com.rehab.system.entity.TreatmentRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TreatmentRecordRepository extends JpaRepository<TreatmentRecord, Long> {

    boolean existsByItemId(Long itemId);

    boolean existsByPatientId(Long patientId);

    void deleteByPatientId(Long patientId);

    List<TreatmentRecord> findByPatientIdOrderByStartDateDesc(Long patientId);
}
