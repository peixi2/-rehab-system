package com.rehab.system.service;

import com.rehab.system.common.BusinessException;
import com.rehab.system.dto.AssessmentTaskRequest;
import com.rehab.system.dto.TaskExecuteRequest;
import com.rehab.system.entity.AssessmentItem;
import com.rehab.system.entity.AssessmentRecord;
import com.rehab.system.entity.AssessmentTask;
import com.rehab.system.entity.Doctor;
import com.rehab.system.entity.Patient;
import com.rehab.system.repository.AssessmentItemRepository;
import com.rehab.system.repository.AssessmentRecordRepository;
import com.rehab.system.repository.AssessmentTaskRepository;
import com.rehab.system.repository.DoctorRepository;
import com.rehab.system.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssessmentTaskService {

    private final AssessmentTaskRepository taskRepository;
    private final PatientRepository patientRepository;
    private final AssessmentItemRepository itemRepository;
    private final DoctorRepository doctorRepository;
    private final AssessmentRecordRepository recordRepository;

    public List<AssessmentTask> list() {
        return taskRepository.findAll();
    }

    public AssessmentTask get(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new BusinessException("评估任务不存在"));
    }

    /** 医师发起评估申请 */
    public AssessmentTask apply(AssessmentTaskRequest request) {
        AssessmentTask task = new AssessmentTask();
        applyRequest(task, request);
        task.setStatus(AssessmentTask.STATUS_PENDING);
        task.setApplyDate(request.getApplyDate() == null ? LocalDate.now() : request.getApplyDate());
        task.setNote(request.getNote());
        return taskRepository.save(task);
    }

    /** 编辑任务（仅待执行状态可修改） */
    public AssessmentTask update(Long id, AssessmentTaskRequest request) {
        AssessmentTask task = get(id);
        if (!AssessmentTask.STATUS_PENDING.equals(task.getStatus())) {
            throw new BusinessException("仅待执行状态的任务可以修改");
        }
        applyRequest(task, request);
        if (request.getApplyDate() != null) {
            task.setApplyDate(request.getApplyDate());
        }
        task.setNote(request.getNote());
        return taskRepository.save(task);
    }

    private void applyRequest(AssessmentTask task, AssessmentTaskRequest request) {
        task.setPatient(patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new BusinessException("病人不存在")));
        task.setItem(itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new BusinessException("量表不存在")));
        task.setDoctor(doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new BusinessException("医生不存在")));
    }

    /** 驳回任务 */
    public AssessmentTask reject(Long id, String reason) {
        AssessmentTask task = get(id);
        if (!AssessmentTask.STATUS_PENDING.equals(task.getStatus())) {
            throw new BusinessException("仅待执行状态的任务可以驳回");
        }
        task.setStatus(AssessmentTask.STATUS_REJECTED);
        task.setRejectReason(reason);
        return taskRepository.save(task);
    }

    /** 治疗师执行任务：自动生成评估记录并完成任务 */
    @Transactional
    public AssessmentRecord execute(Long id, TaskExecuteRequest request) {
        AssessmentTask task = get(id);
        if (!AssessmentTask.STATUS_PENDING.equals(task.getStatus())) {
            throw new BusinessException("仅待执行状态的任务可以执行");
        }
        if (request.getTherapistId() == null) {
            throw new BusinessException("请选择执行治疗师");
        }
        Doctor therapist = doctorRepository.findById(request.getTherapistId())
                .orElseThrow(() -> new BusinessException("治疗师不存在"));

        AssessmentRecord record = new AssessmentRecord();
        record.setPatient(task.getPatient());
        record.setItem(task.getItem());
        record.setDoctor(therapist);
        record.setScore(request.getScore());
        record.setResult(request.getResult());
        record.setDetail(request.getDetail());
        record.setAssessDate(request.getAssessDate() == null ? LocalDate.now() : request.getAssessDate());
        record = recordRepository.save(record);

        task.setStatus(AssessmentTask.STATUS_COMPLETED);
        task.setTherapist(therapist);
        task.setExecuteDate(record.getAssessDate());
        task.setRecordId(record.getId());
        taskRepository.save(task);
        return record;
    }

    /** 删除任务；已执行生成评估记录的任务一并删除其记录 */
    @Transactional
    public void delete(Long id) {
        AssessmentTask task = get(id);
        if (task.getRecordId() != null) {
            recordRepository.findById(task.getRecordId())
                    .ifPresent(recordRepository::delete);
        }
        taskRepository.delete(task);
    }
}
