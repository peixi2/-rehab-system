package com.rehab.system.controller;

import com.rehab.system.common.ApiResponse;
import com.rehab.system.common.AuditLog;
import com.rehab.system.common.SecurityExpressions;
import com.rehab.system.dto.ScaleAnswerRequest;
import com.rehab.system.entity.ScaleItem;
import com.rehab.system.service.ScaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/scales")
@RequiredArgsConstructor
public class ScaleController {

    private final ScaleService scaleService;

    @GetMapping("/{id}/detail")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        return ApiResponse.ok(scaleService.detail(id));
    }

    @PostMapping("/{id}/score")
    public ApiResponse<Map<String, Object>> score(@PathVariable Long id, @RequestBody ScaleAnswerRequest request) {
        return ApiResponse.ok(scaleService.score(id, request));
    }

    @PutMapping("/{id}/items")
    @PreAuthorize(SecurityExpressions.WRITE_BASE)
    @AuditLog(module = "量表库管理", action = "配置条目")
    public ApiResponse<Void> saveItems(@PathVariable Long id, @RequestBody List<ScaleItem> items) {
        scaleService.saveItems(id, items);
        return ApiResponse.ok();
    }
}
