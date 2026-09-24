package com.rehab.system.controller;

import com.rehab.system.common.ApiResponse;
import com.rehab.system.common.AuditLog;
import com.rehab.system.common.SecurityExpressions;
import com.rehab.system.entity.AssessmentItem;
import com.rehab.system.service.AssessmentItemService;
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
@RequestMapping("/api/assessment-items")
@RequiredArgsConstructor
public class AssessmentItemController {

    private final AssessmentItemService itemService;

    @GetMapping
    public ApiResponse<List<AssessmentItem>> list() {
        return ApiResponse.ok(itemService.list());
    }

    @GetMapping("/{id}")
    public ApiResponse<AssessmentItem> get(@PathVariable Long id) {
        return ApiResponse.ok(itemService.get(id));
    }

    @PostMapping
    @PreAuthorize(SecurityExpressions.WRITE_BASE)
    @AuditLog(module = "量表库管理", action = "新增")
    public ApiResponse<AssessmentItem> create(@RequestBody AssessmentItem item) {
        return ApiResponse.ok(itemService.create(item));
    }

    @PutMapping("/{id}")
    @PreAuthorize(SecurityExpressions.WRITE_BASE)
    @AuditLog(module = "量表库管理", action = "修改")
    public ApiResponse<AssessmentItem> update(@PathVariable Long id, @RequestBody AssessmentItem item) {
        return ApiResponse.ok(itemService.update(id, item));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(SecurityExpressions.WRITE_BASE)
    @AuditLog(module = "量表库管理", action = "删除")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        itemService.delete(id);
        return ApiResponse.ok();
    }
}
