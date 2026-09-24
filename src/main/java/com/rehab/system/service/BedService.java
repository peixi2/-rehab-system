package com.rehab.system.service;

import com.rehab.system.common.BusinessException;
import com.rehab.system.entity.Bed;
import com.rehab.system.entity.Patient;
import com.rehab.system.repository.BedRepository;
import com.rehab.system.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BedService {

    private final BedRepository bedRepository;
    private final PatientRepository patientRepository;

    public List<Bed> list() {
        return bedRepository.findAll();
    }

    public Bed get(Long id) {
        return bedRepository.findById(id)
                .orElseThrow(() -> new BusinessException("床位不存在"));
    }

    public Bed create(Bed bed) {
        checkBedNo(bed.getBedNo());
        bed.setId(null);
        if (bed.getStatus() == null) {
            bed.setStatus(Bed.STATUS_FREE);
        }
        return bedRepository.save(bed);
    }

    public Bed update(Long id, Bed bed) {
        checkBedNo(bed.getBedNo());
        Bed db = get(id);
        db.setBedNo(bed.getBedNo());
        db.setRoomNo(bed.getRoomNo());
        return bedRepository.save(db);
    }

    public void delete(Long id) {
        Bed bed = get(id);
        if (patientRepository.existsByBedId(id)) {
            throw new BusinessException("床位 " + bed.getBedNo() + " 上有病人，请先办理退床再删除");
        }
        bedRepository.deleteById(id);
    }

    @Transactional
    public Bed assign(Long bedId, Long patientId) {
        Bed bed = get(bedId);
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new BusinessException("病人不存在"));

        if (patient.getBed() != null && patient.getBed().getId().equals(bedId)) {
            return bed;
        }
        if (Bed.STATUS_OCCUPIED.equals(bed.getStatus())) {
            throw new BusinessException("床位 " + bed.getBedNo() + " 已被占用，无法分配");
        }
        if (patient.getBed() != null) {
            patient.getBed().setStatus(Bed.STATUS_FREE);
        }
        bed.setStatus(Bed.STATUS_OCCUPIED);
        patient.setBed(bed);
        patientRepository.save(patient);
        return bedRepository.save(bed);
    }

    @Transactional
    public Bed release(Long bedId) {
        Bed bed = get(bedId);
        patientRepository.findByBedId(bedId).ifPresent(patient -> {
            patient.setBed(null);
            patientRepository.save(patient);
        });
        bed.setStatus(Bed.STATUS_FREE);
        return bedRepository.save(bed);
    }

    private void checkBedNo(String bedNo) {
        if (bedNo == null || bedNo.isBlank()) {
            throw new BusinessException("床位号不能为空");
        }
    }
}
