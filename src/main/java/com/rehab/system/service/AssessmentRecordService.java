package com.rehab.system.service;

import com.rehab.system.common.BusinessException;
import com.rehab.system.dto.AssessmentRecordRequest;
import com.rehab.system.entity.AssessmentItem;
import com.rehab.system.entity.AssessmentRecord;
import com.rehab.system.entity.Doctor;
import com.rehab.system.entity.Patient;
import com.rehab.system.repository.AssessmentItemRepository;
import com.rehab.system.repository.AssessmentRecordRepository;
import com.rehab.system.repository.DoctorRepository;
import com.rehab.system.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssessmentRecordService {

    private final AssessmentRecordRepository recordRepository;
    private final PatientRepository patientRepository;
    private final AssessmentItemRepository itemRepository;
    private final DoctorRepository doctorRepository;

    public List<AssessmentRecord> list() {
        return recordRepository.findAll();
    }

    public List<AssessmentRecord> listByPatient(Long patientId) {
        return recordRepository.findByPatientIdOrderByAssessDateAsc(patientId);
    }

    public AssessmentRecord get(Long id) {
        return recordRepository.findById(id)
                .orElseThrow(() -> new BusinessException("评估记录不存在"));
    }

    public AssessmentRecord create(AssessmentRecordRequest request) {
        AssessmentRecord record = new AssessmentRecord();
        applyRequest(record, request);
        if (record.getAssessDate() == null) {
            record.setAssessDate(LocalDate.now());
        }
        return recordRepository.save(record);
    }

    public AssessmentRecord update(Long id, AssessmentRecordRequest request) {
        AssessmentRecord record = get(id);
        applyRequest(record, request);
        return recordRepository.save(record);
    }

    public void delete(Long id) {
        get(id);
        recordRepository.deleteById(id);
    }

    private void applyRequest(AssessmentRecord record, AssessmentRecordRequest request) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new BusinessException("病人不存在"));
        AssessmentItem item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new BusinessException("评估项目不存在"));
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new BusinessException("医生不存在"));

        record.setPatient(patient);
        record.setItem(item);
        record.setDoctor(doctor);
        record.setScore(request.getScore());
        record.setResult(request.getResult());
        record.setAssessDate(request.getAssessDate());
        record.setDetail(request.getDetail());
    }
}
