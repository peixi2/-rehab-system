package com.rehab.system.controller;

import com.rehab.system.common.ApiResponse;
import com.rehab.system.common.AuditLog;
import com.rehab.system.common.BusinessException;
import com.rehab.system.common.SecurityExpressions;
import com.rehab.system.dto.PatientRequest;
import com.rehab.system.entity.Patient;
import com.rehab.system.service.CurrentUserService;
import com.rehab.system.service.PatientService;
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
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ApiResponse<List<Patient>> list() {
        return ApiResponse.ok(patientService.list());
    }

    /** 患者端查看自己的信息（含床位） */
    @GetMapping("/me")
    public ApiResponse<Patient> me(Authentication authentication) {
        Long patientId = currentUserService.patientIdIfPatient(authentication);
        if (patientId == null) {
            throw new BusinessException("当前账号未绑定病人，请联系管理员绑定");
        }
        return ApiResponse.ok(patientService.get(patientId));
    }

    @GetMapping("/{id}")
    public ApiResponse<Patient> get(@PathVariable Long id) {
        return ApiResponse.ok(patientService.get(id));
    }

    @PostMapping
    @PreAuthorize(SecurityExpressions.WRITE_BASE)
    @AuditLog(module = "患者管理", action = "新增")
    public ApiResponse<Patient> create(@Valid @RequestBody PatientRequest request) {
        return ApiResponse.ok(patientService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize(SecurityExpressions.WRITE_BASE)
    @AuditLog(module = "患者管理", action = "修改")
    public ApiResponse<Patient> update(@PathVariable Long id, @Valid @RequestBody PatientRequest request) {
        return ApiResponse.ok(patientService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(SecurityExpressions.WRITE_BASE)
    @AuditLog(module = "患者管理", action = "删除")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        patientService.delete(id);
        return ApiResponse.ok();
    }
}
