package com.rehab.system.service;

import com.rehab.system.common.BusinessException;
import com.rehab.system.dto.PatientRequest;
import com.rehab.system.entity.Bed;
import com.rehab.system.entity.Doctor;
import com.rehab.system.entity.Patient;
import com.rehab.system.repository.AssessmentRecordRepository;
import com.rehab.system.repository.BedRepository;
import com.rehab.system.repository.DoctorRepository;
import com.rehab.system.repository.PatientRepository;
import com.rehab.system.repository.TreatmentRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final BedRepository bedRepository;
    private final AssessmentRecordRepository assessmentRecordRepository;
    private final TreatmentRecordRepository treatmentRecordRepository;

    public List<Patient> list() {
        return patientRepository.findAll();
    }

    public Patient get(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new BusinessException("病人不存在"));
    }

    @Transactional
    public Patient create(PatientRequest request) {
        Patient patient = new Patient();
        applyRequest(patient, request);
        return patientRepository.save(patient);
    }

    @Transactional
    public Patient update(Long id, PatientRequest request) {
        Patient patient = get(id);
        applyRequest(patient, request);
        return patientRepository.save(patient);
    }

    @Transactional
    public void delete(Long id) {
        Patient patient = get(id);
        if (patient.getBed() != null) {
            patient.getBed().setStatus(Bed.STATUS_FREE);
        }
        assessmentRecordRepository.deleteByPatientId(id);
        treatmentRecordRepository.deleteByPatientId(id);
        patientRepository.delete(patient);
    }

    private void applyRequest(Patient patient, PatientRequest request) {
        patient.setName(request.getName());
        patient.setGender(request.getGender());
        patient.setAge(request.getAge());
        patient.setPhone(request.getPhone());
        patient.setDiagnosis(request.getDiagnosis());
        patient.setAdmissionDate(request.getAdmissionDate());

        if (request.getDoctorId() == null) {
            patient.setDoctor(null);
        } else if (patient.getDoctor() == null || !patient.getDoctor().getId().equals(request.getDoctorId())) {
            Doctor doctor = doctorRepository.findById(request.getDoctorId())
                    .orElseThrow(() -> new BusinessException("医生不存在"));
            patient.setDoctor(doctor);
        }

        applyBed(patient, request.getBedId());
    }

    private void applyBed(Patient patient, Long bedId) {
        if (bedId == null) {
            if (patient.getBed() != null) {
                patient.getBed().setStatus(Bed.STATUS_FREE);
                patient.setBed(null);
            }
            return;
        }
        if (patient.getBed() != null && patient.getBed().getId().equals(bedId)) {
            return;
        }
        Bed bed = bedRepository.findById(bedId)
                .orElseThrow(() -> new BusinessException("床位不存在"));
        if (Bed.STATUS_OCCUPIED.equals(bed.getStatus())) {
            throw new BusinessException("床位 " + bed.getBedNo() + " 已被占用");
        }
        if (patient.getBed() != null) {
            patient.getBed().setStatus(Bed.STATUS_FREE);
        }
        bed.setStatus(Bed.STATUS_OCCUPIED);
        patient.setBed(bed);
    }
}
