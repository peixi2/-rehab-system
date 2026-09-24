package com.rehab.system.service;

import com.rehab.system.common.BusinessException;
import com.rehab.system.dto.TreatmentRecordRequest;
import com.rehab.system.entity.Doctor;
import com.rehab.system.entity.Patient;
import com.rehab.system.entity.TreatmentItem;
import com.rehab.system.entity.TreatmentRecord;
import com.rehab.system.repository.DoctorRepository;
import com.rehab.system.repository.PatientRepository;
import com.rehab.system.repository.TreatmentItemRepository;
import com.rehab.system.repository.TreatmentRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TreatmentRecordService {

    private final TreatmentRecordRepository recordRepository;
    private final PatientRepository patientRepository;
    private final TreatmentItemRepository itemRepository;
    private final DoctorRepository doctorRepository;

    public List<TreatmentRecord> list() {
        return recordRepository.findAll();
    }

    public List<TreatmentRecord> listByPatient(Long patientId) {
        return recordRepository.findByPatientIdOrderByStartDateDesc(patientId);
    }

    public TreatmentRecord get(Long id) {
        return recordRepository.findById(id)
                .orElseThrow(() -> new BusinessException("治疗记录不存在"));
    }

    public TreatmentRecord create(TreatmentRecordRequest request) {
        TreatmentRecord record = new TreatmentRecord();
        applyRequest(record, request);
        if (record.getStartDate() == null) {
            record.setStartDate(LocalDate.now());
        }
        return recordRepository.save(record);
    }

    public TreatmentRecord update(Long id, TreatmentRecordRequest request) {
        TreatmentRecord record = get(id);
        applyRequest(record, request);
        return recordRepository.save(record);
    }

    public void delete(Long id) {
        get(id);
        recordRepository.deleteById(id);
    }

    private void applyRequest(TreatmentRecord record, TreatmentRecordRequest request) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new BusinessException("病人不存在"));
        TreatmentItem item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new BusinessException("治疗项目不存在"));
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new BusinessException("医生不存在"));

        record.setPatient(patient);
        record.setItem(item);
        record.setDoctor(doctor);
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            record.setStatus(request.getStatus());
        }
        record.setStartDate(request.getStartDate());
        record.setEndDate(request.getEndDate());
        record.setNote(request.getNote());
    }
}
