package com.rehab.system.controller;

import com.rehab.system.common.ApiResponse;
import com.rehab.system.common.AuditLog;
import com.rehab.system.common.SecurityExpressions;
import com.rehab.system.entity.Doctor;
import com.rehab.system.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping
    public ApiResponse<List<Doctor>> list() {
        return ApiResponse.ok(doctorService.list());
    }

    @GetMapping("/{id}")
    public ApiResponse<Doctor> get(@PathVariable Long id) {
        return ApiResponse.ok(doctorService.get(id));
    }

    @PostMapping
    @PreAuthorize(SecurityExpressions.WRITE_BASE)
    @AuditLog(module = "医生管理", action = "新增")
    public ApiResponse<Doctor> create(@RequestBody Doctor doctor) {
        return ApiResponse.ok(doctorService.create(doctor));
    }

    @PutMapping("/{id}")
    @PreAuthorize(SecurityExpressions.WRITE_BASE)
    @AuditLog(module = "医生管理", action = "修改")
    public ApiResponse<Doctor> update(@PathVariable Long id, @RequestBody Doctor doctor) {
        return ApiResponse.ok(doctorService.update(id, doctor));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(SecurityExpressions.WRITE_BASE)
    @AuditLog(module = "医生管理", action = "删除")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        doctorService.delete(id);
        return ApiResponse.ok();
    }
}
