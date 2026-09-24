package com.rehab.system.controller;

import com.rehab.system.common.ApiResponse;
import com.rehab.system.common.BusinessException;
import com.rehab.system.common.RoleConstants;
import com.rehab.system.common.SecurityExpressions;
import com.rehab.system.dto.LoginRequest;
import com.rehab.system.entity.AuditLog;
import com.rehab.system.entity.SysUser;
import com.rehab.system.repository.AuditLogRepository;
import com.rehab.system.repository.PatientRepository;
import com.rehab.system.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/users")
    @PreAuthorize(SecurityExpressions.ADMIN_ONLY)
    public ApiResponse<List<SysUser>> listUsers() {
        List<SysUser> users = userRepository.findAll(Sort.by("id"));
        users.forEach(u -> u.setPassword(null));
        return ApiResponse.ok(users);
    }

    @PostMapping("/users")
    @PreAuthorize(SecurityExpressions.ADMIN_ONLY)
    public ApiResponse<SysUser> createUser(@Valid @RequestBody LoginRequest request,
                                           @RequestParam String role,
                                           @RequestParam(required = false) String realName,
                                           @RequestParam(required = false) Long patientId) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        if (Arrays.stream(RoleConstants.ALL).noneMatch(r -> r.equals(role))) {
            throw new BusinessException("非法角色：" + role);
        }
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setRealName(realName);
        user.setEnabled(true);
        if (patientId != null) {
            validateBindPatient(user, patientId);
        }
        userRepository.save(user);
        user.setPassword(null);
        return ApiResponse.ok(user);
    }

    /** 绑定/解绑患者账号对应的病人（patientId 为空则解绑） */
    @PutMapping("/users/{id}/patient")
    @PreAuthorize(SecurityExpressions.ADMIN_ONLY)
    public ApiResponse<Void> bindPatient(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        SysUser user = userRepository.findById(id).orElseThrow(() -> new BusinessException("账号不存在"));
        Object pid = body.get("patientId");
        if (pid == null) {
            user.setPatientId(null);
        } else {
            validateBindPatient(user, Long.valueOf(pid.toString()));
        }
        userRepository.save(user);
        return ApiResponse.ok();
    }

    private void validateBindPatient(SysUser user, Long patientId) {
        if (!RoleConstants.PATIENT.equals(user.getRole())) {
            throw new BusinessException("仅患者角色账号可绑定病人");
        }
        if (!patientRepository.existsById(patientId)) {
            throw new BusinessException("病人不存在");
        }
        user.setPatientId(patientId);
    }

    @PutMapping("/users/{id}/status")
    @PreAuthorize(SecurityExpressions.ADMIN_ONLY)
    public ApiResponse<Void> toggleStatus(@PathVariable Long id, @RequestParam Boolean enabled) {
        SysUser user = userRepository.findById(id).orElseThrow(() -> new BusinessException("账号不存在"));
        user.setEnabled(enabled);
        userRepository.save(user);
        return ApiResponse.ok();
    }

    @PutMapping("/users/{id}/password")
    @PreAuthorize(SecurityExpressions.ADMIN_ONLY)
    public ApiResponse<Void> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        SysUser user = userRepository.findById(id).orElseThrow(() -> new BusinessException("账号不存在"));
        String newPassword = body.get("password");
        if (newPassword == null || newPassword.length() < 6) {
            throw new BusinessException("密码至少 6 位");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return ApiResponse.ok();
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize(SecurityExpressions.ADMIN_ONLY)
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        SysUser user = userRepository.findById(id).orElseThrow(() -> new BusinessException("账号不存在"));
        if ("admin".equals(user.getUsername())) {
            throw new BusinessException("内置管理员账号不可删除");
        }
        userRepository.delete(user);
        return ApiResponse.ok();
    }

    @GetMapping("/audit-logs")
    @PreAuthorize(SecurityExpressions.ADMIN_ONLY)
    public ApiResponse<List<AuditLog>> listAuditLogs(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "50") int size) {
        return ApiResponse.ok(auditLogRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"))).getContent());
    }
}
