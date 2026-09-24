package com.rehab.system.service;

import com.rehab.system.common.BusinessException;
import com.rehab.system.entity.Doctor;
import com.rehab.system.repository.DoctorRepository;
import com.rehab.system.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    public List<Doctor> list() {
        return doctorRepository.findAll();
    }

    public Doctor get(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("医生不存在"));
    }

    public Doctor create(Doctor doctor) {
        checkName(doctor.getName());
        doctor.setId(null);
        return doctorRepository.save(doctor);
    }

    public Doctor update(Long id, Doctor doctor) {
        checkName(doctor.getName());
        Doctor db = get(id);
        db.setName(doctor.getName());
        db.setGender(doctor.getGender());
        db.setTitle(doctor.getTitle());
        db.setDepartment(doctor.getDepartment());
        db.setPhone(doctor.getPhone());
        return doctorRepository.save(db);
    }

    public void delete(Long id) {
        get(id);
        if (patientRepository.existsByDoctorId(id)) {
            throw new BusinessException("该医生名下还有病人，请先转移病人再删除");
        }
        doctorRepository.deleteById(id);
    }

    private void checkName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessException("医生姓名不能为空");
        }
    }
}
