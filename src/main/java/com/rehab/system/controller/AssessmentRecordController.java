package com.rehab.system.controller;

import com.rehab.system.common.ApiResponse;
import com.rehab.system.common.AuditLog;
import com.rehab.system.common.SecurityExpressions;
import com.rehab.system.dto.AssessmentRecordRequest;
import com.rehab.system.entity.AssessmentRecord;
import com.rehab.system.service.AssessmentRecordService;
import com.rehab.system.service.CurrentUserService;
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
@RequestMapping("/api/assessment-records")
@RequiredArgsConstructor
public class AssessmentRecordController {

    private final AssessmentRecordService recordService;
    private final CurrentUserService currentUserService;

    /** 患者角色仅能看到本人的评估记录 */
    @GetMapping
    public ApiResponse<List<AssessmentRecord>> list(Authentication authentication) {
        Long patientId = currentUserService.patientIdIfPatient(authentication);
        return ApiResponse.ok(patientId == null ? recordService.list() : recordService.listByPatient(patientId));
    }

    @GetMapping("/{id}")
    public ApiResponse<AssessmentRecord> get(@PathVariable Long id) {
        return ApiResponse.ok(recordService.get(id));
    }

    @PostMapping
    @PreAuthorize(SecurityExpressions.WRITE_RECORD)
    @AuditLog(module = "评估记录", action = "新增")
    public ApiResponse<AssessmentRecord> create(@Valid @RequestBody AssessmentRecordRequest request) {
        return ApiResponse.ok(recordService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize(SecurityExpressions.WRITE_RECORD)
    @AuditLog(module = "评估记录", action = "修改")
    public ApiResponse<AssessmentRecord> update(@PathVariable Long id, @Valid @RequestBody AssessmentRecordRequest request) {
        return ApiResponse.ok(recordService.update(id, request));
    }

    // 医疗合规：原始评估数据仅系统管理员可删除
    @DeleteMapping("/{id}")
    @PreAuthorize(SecurityExpressions.ADMIN_ONLY)
    @AuditLog(module = "评估记录", action = "删除")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        recordService.delete(id);
        return ApiResponse.ok();
    }
}
