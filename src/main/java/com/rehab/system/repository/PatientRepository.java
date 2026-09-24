package com.rehab.system.repository;

import com.rehab.system.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    boolean existsByDoctorId(Long doctorId);

    boolean existsByBedId(Long bedId);

    Optional<Patient> findByBedId(Long bedId);

    Optional<Patient> findByName(String name);

    @Query("select p.diagnosis, count(p) from Patient p group by p.diagnosis")
    List<Object[]> countGroupByDiagnosis();
}
