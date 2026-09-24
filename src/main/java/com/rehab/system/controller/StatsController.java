package com.rehab.system.controller;

import com.rehab.system.common.ApiResponse;
import com.rehab.system.entity.AssessmentRecord;
import com.rehab.system.entity.Bed;
import com.rehab.system.repository.AssessmentItemRepository;
import com.rehab.system.repository.AssessmentRecordRepository;
import com.rehab.system.repository.BedRepository;
import com.rehab.system.repository.DoctorRepository;
import com.rehab.system.repository.PatientRepository;
import com.rehab.system.repository.TreatmentItemRepository;
import com.rehab.system.repository.TreatmentRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final BedRepository bedRepository;
    private final AssessmentItemRepository assessmentItemRepository;
    private final TreatmentItemRepository treatmentItemRepository;
    private final AssessmentRecordRepository assessmentRecordRepository;
    private final TreatmentRecordRepository treatmentRecordRepository;

    @GetMapping
    public ApiResponse<Map<String, Object>> stats() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("patientCount", patientRepository.count());
        data.put("doctorCount", doctorRepository.count());
        data.put("bedCount", bedRepository.count());
        data.put("freeBedCount", bedRepository.countByStatus(Bed.STATUS_FREE));
        data.put("occupiedBedCount", bedRepository.countByStatus(Bed.STATUS_OCCUPIED));
        data.put("assessmentItemCount", assessmentItemRepository.count());
        data.put("treatmentItemCount", treatmentItemRepository.count());
        data.put("assessmentRecordCount", assessmentRecordRepository.count());
        data.put("treatmentRecordCount", treatmentRecordRepository.count());
        return ApiResponse.ok(data);
    }

    /** 患者康复趋势：某患者在某量表上的历次得分（按时间升序），itemId 为空时返回全部 */
    @GetMapping("/patient/{patientId}/trend")
    public ApiResponse<List<Map<String, Object>>> patientTrend(@PathVariable Long patientId,
                                                              @RequestParam(required = false) Long itemId) {
        List<AssessmentRecord> records = itemId == null
                ? assessmentRecordRepository.findByPatientIdOrderByAssessDateAsc(patientId)
                : assessmentRecordRepository.findByPatientIdAndItemIdOrderByAssessDateAsc(patientId, itemId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (AssessmentRecord record : records) {
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("assessDate", record.getAssessDate() == null ? null : record.getAssessDate().toString());
            point.put("score", record.getScore());
            point.put("result", record.getResult());
            point.put("itemName", record.getItem() == null ? null : record.getItem().getName());
            point.put("doctorName", record.getDoctor() == null ? null : record.getDoctor().getName());
            result.add(point);
        }
        return ApiResponse.ok(result);
    }

    /** 病种分布统计 */
    @GetMapping("/diagnosis")
    public ApiResponse<List<Map<String, Object>>> diagnosisStats() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : patientRepository.countGroupByDiagnosis()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("diagnosis", row[0]);
            item.put("count", row[1]);
            result.add(item);
        }
        return ApiResponse.ok(result);
    }
}
