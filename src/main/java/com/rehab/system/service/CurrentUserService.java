package com.rehab.system.service;

import com.rehab.system.common.BusinessException;
import com.rehab.system.common.RoleConstants;
import com.rehab.system.entity.SysUser;
import com.rehab.system.repository.UserRepository;
import com.rehab.system.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    public SysUser currentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof JwtAuthFilter.LoginUser loginUser)) {
            throw new BusinessException("未登录");
        }
        return userRepository.findById(loginUser.id())
                .orElseThrow(() -> new BusinessException("账号不存在"));
    }

    /** 当前用户为患者角色时返回其绑定的病人ID，否则返回 null */
    public Long patientIdIfPatient(Authentication authentication) {
        SysUser user = currentUser(authentication);
        return RoleConstants.PATIENT.equals(user.getRole()) ? user.getPatientId() : null;
    }
}
