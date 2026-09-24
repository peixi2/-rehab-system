package com.rehab.system.controller;

import com.rehab.system.common.ApiResponse;
import com.rehab.system.common.AuditLog;
import com.rehab.system.common.SecurityExpressions;
import com.rehab.system.dto.TreatmentRecordRequest;
import com.rehab.system.entity.TreatmentRecord;
import com.rehab.system.service.CurrentUserService;
import com.rehab.system.service.TreatmentRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/treatment-records")
@RequiredArgsConstructor
public class TreatmentRecordController {

    private final TreatmentRecordService recordService;
    private final CurrentUserService currentUserService;

    /** 患者角色仅能看到本人的治疗记录 */
    @GetMapping
    public ApiResponse<List<TreatmentRecord>> list(Authentication authentication) {
        Long patientId = currentUserService.patientIdIfPatient(authentication);
        return ApiResponse.ok(patientId == null ? recordService.list() : recordService.listByPatient(patientId));
    }

    @GetMapping("/{id}")
    public ApiResponse<TreatmentRecord> get(@PathVariable Long id) {
        return ApiResponse.ok(recordService.get(id));
    }

    @PostMapping
    @PreAuthorize(SecurityExpressions.WRITE_RECORD)
    @AuditLog(module = "治疗记录", action = "新增")
    public ApiResponse<TreatmentRecord> create(@Valid @RequestBody TreatmentRecordRequest request) {
        return ApiResponse.ok(recordService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize(SecurityExpressions.WRITE_RECORD)
    @AuditLog(module = "治疗记录", action = "修改")
    public ApiResponse<TreatmentRecord> update(@PathVariable Long id, @Valid @RequestBody TreatmentRecordRequest request) {
        return ApiResponse.ok(recordService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(SecurityExpressions.WRITE_RECORD)
    @AuditLog(module = "治疗记录", action = "删除")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        recordService.delete(id);
        return ApiResponse.ok();
    }
}
