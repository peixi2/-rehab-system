package com.rehab.system.aspect;

import com.rehab.system.common.AuditLog;
import com.rehab.system.repository.AuditLogRepository;
import com.rehab.system.security.JwtAuthFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class AuditAspect {

    private final AuditLogRepository auditLogRepository;

    public AuditAspect(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Around("@annotation(auditAnnotation)")
    public Object around(ProceedingJoinPoint joinPoint, AuditLog auditAnnotation) throws Throwable {
        Object result = joinPoint.proceed();
        try {
            saveLog(joinPoint, auditAnnotation);
        } catch (Exception e) {
            log.warn("记录审计日志失败", e);
        }
        return result;
    }

    private void saveLog(ProceedingJoinPoint joinPoint, AuditLog auditAnnotation) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        com.rehab.system.entity.AuditLog entity = new com.rehab.system.entity.AuditLog();
        if (auth != null && auth.getPrincipal() instanceof JwtAuthFilter.LoginUser loginUser) {
            entity.setUserId(loginUser.id());
            entity.setUsername(loginUser.username());
            entity.setRole(loginUser.role());
        } else {
            entity.setUsername("anonymous");
        }
        entity.setModule(auditAnnotation.module());
        entity.setAction(auditAnnotation.action());
        String detail = ((MethodSignature) joinPoint.getSignature()).getMethod().getName() + " "
                + Arrays.toString(joinPoint.getArgs());
        entity.setDetail(detail.length() > 490 ? detail.substring(0, 490) : detail);
        entity.setIp(getClientIp());
        auditLogRepository.save(entity);
    }

    private String getClientIp() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return null;
        }
        HttpServletRequest request = attrs.getRequest();
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
