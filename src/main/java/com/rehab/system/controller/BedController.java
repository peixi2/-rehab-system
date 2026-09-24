package com.rehab.system.controller;

import com.rehab.system.common.ApiResponse;
import com.rehab.system.common.AuditLog;
import com.rehab.system.common.SecurityExpressions;
import com.rehab.system.entity.Bed;
import com.rehab.system.service.BedService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/beds")
@RequiredArgsConstructor
public class BedController {

    private final BedService bedService;

    @GetMapping
    public ApiResponse<List<Bed>> list() {
        return ApiResponse.ok(bedService.list());
    }

    @GetMapping("/{id}")
    public ApiResponse<Bed> get(@PathVariable Long id) {
        return ApiResponse.ok(bedService.get(id));
    }

    @PostMapping
    @PreAuthorize(SecurityExpressions.WRITE_BASE)
    @AuditLog(module = "床位管理", action = "新增")
    public ApiResponse<Bed> create(@RequestBody Bed bed) {
        return ApiResponse.ok(bedService.create(bed));
    }

    @PutMapping("/{id}")
    @PreAuthorize(SecurityExpressions.WRITE_BASE)
    @AuditLog(module = "床位管理", action = "修改")
    public ApiResponse<Bed> update(@PathVariable Long id, @RequestBody Bed bed) {
        return ApiResponse.ok(bedService.update(id, bed));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(SecurityExpressions.WRITE_BASE)
    @AuditLog(module = "床位管理", action = "删除")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        bedService.delete(id);
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize(SecurityExpressions.WRITE_BASE)
    @AuditLog(module = "床位管理", action = "分配")
    public ApiResponse<Bed> assign(@PathVariable Long id, @RequestParam Long patientId) {
        return ApiResponse.ok(bedService.assign(id, patientId));
    }

    @PostMapping("/{id}/release")
    @PreAuthorize(SecurityExpressions.WRITE_BASE)
    @AuditLog(module = "床位管理", action = "释放")
    public ApiResponse<Bed> release(@PathVariable Long id) {
        return ApiResponse.ok(bedService.release(id));
    }
}
