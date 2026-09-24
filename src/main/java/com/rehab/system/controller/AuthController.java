package com.rehab.system.controller;

import com.rehab.system.common.ApiResponse;
import com.rehab.system.common.BusinessException;
import com.rehab.system.dto.LoginRequest;
import com.rehab.system.entity.SysUser;
import com.rehab.system.repository.UserRepository;
import com.rehab.system.security.JwtAuthFilter;
import com.rehab.system.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        SysUser user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException("用户名或密码错误"));
        if (!Boolean.TRUE.equals(user.getEnabled())) {
            throw new BusinessException("账号已被停用，请联系管理员");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("token", token);
        Map<String, Object> userInfo = new LinkedHashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("role", user.getRole());
        userInfo.put("realName", user.getRealName());
        userInfo.put("patientId", user.getPatientId());
        data.put("user", userInfo);
        return ApiResponse.ok(data);
    }

    @GetMapping("/me")
    public ApiResponse<Map<String, Object>> me(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof JwtAuthFilter.LoginUser loginUser)) {
            throw new BusinessException("未登录");
        }
        SysUser user = userRepository.findById(loginUser.id())
                .orElseThrow(() -> new BusinessException("账号不存在"));
        Map<String, Object> userInfo = new LinkedHashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("role", user.getRole());
        userInfo.put("realName", user.getRealName());
        userInfo.put("patientId", user.getPatientId());
        return ApiResponse.ok(userInfo);
    }
}
