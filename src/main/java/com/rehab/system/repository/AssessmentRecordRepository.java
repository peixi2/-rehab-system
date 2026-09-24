package com.rehab.system.repository;

import com.rehab.system.entity.AssessmentRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssessmentRecordRepository extends JpaRepository<AssessmentRecord, Long> {

    boolean existsByItemId(Long itemId);

    boolean existsByPatientId(Long patientId);

    long countByPatientId(Long patientId);

    void deleteByPatientId(Long patientId);

    List<AssessmentRecord> findByPatientIdAndItemIdOrderByAssessDateAsc(Long patientId, Long itemId);

    List<AssessmentRecord> findByPatientIdOrderByAssessDateAsc(Long patientId);
}
