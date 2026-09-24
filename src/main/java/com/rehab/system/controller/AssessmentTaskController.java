package com.rehab.system.controller;

import com.rehab.system.common.ApiResponse;
import com.rehab.system.common.AuditLog;
import com.rehab.system.common.SecurityExpressions;
import com.rehab.system.dto.AssessmentTaskRequest;
import com.rehab.system.dto.TaskExecuteRequest;
import com.rehab.system.entity.AssessmentRecord;
import com.rehab.system.entity.AssessmentTask;
import com.rehab.system.service.AssessmentTaskService;
import jakarta.validation.Valid;
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
import java.util.Map;

@RestController
@RequestMapping("/api/assessment-tasks")
@RequiredArgsConstructor
public class AssessmentTaskController {

    private final AssessmentTaskService taskService;

    @GetMapping
    public ApiResponse<List<AssessmentTask>> list() {
        return ApiResponse.ok(taskService.list());
    }

    @GetMapping("/{id}")
    public ApiResponse<AssessmentTask> get(@PathVariable Long id) {
        return ApiResponse.ok(taskService.get(id));
    }

    /** 医师发起评估申请 */
    @PostMapping
    @PreAuthorize(SecurityExpressions.WRITE_RECORD)
    @AuditLog(module = "评估任务", action = "发起申请")
    public ApiResponse<AssessmentTask> apply(@Valid @RequestBody AssessmentTaskRequest request) {
        return ApiResponse.ok(taskService.apply(request));
    }

    /** 驳回任务 */
    @PutMapping("/{id}/reject")
    @PreAuthorize(SecurityExpressions.WRITE_RECORD)
    @AuditLog(module = "评估任务", action = "驳回")
    public ApiResponse<AssessmentTask> reject(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ApiResponse.ok(taskService.reject(id, body.get("reason")));
    }

    /** 编辑任务（仅待执行状态可修改） */
    @PutMapping("/{id}")
    @PreAuthorize(SecurityExpressions.WRITE_RECORD)
    @AuditLog(module = "评估任务", action = "修改")
    public ApiResponse<AssessmentTask> update(@PathVariable Long id, @Valid @RequestBody AssessmentTaskRequest request) {
        return ApiResponse.ok(taskService.update(id, request));
    }

    /** 治疗师执行任务：填写量表并自动生成评估记录 */
    @PostMapping("/{id}/execute")
    @PreAuthorize(SecurityExpressions.WRITE_RECORD)
    @AuditLog(module = "评估任务", action = "执行")
    public ApiResponse<AssessmentRecord> execute(@PathVariable Long id, @RequestBody TaskExecuteRequest request) {
        return ApiResponse.ok(taskService.execute(id, request));
    }

    /** 删除任务（已执行的任务会连带删除其生成的评估记录） */
    @DeleteMapping("/{id}")
    @PreAuthorize(SecurityExpressions.WRITE_RECORD)
    @AuditLog(module = "评估任务", action = "删除")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        taskService.delete(id);
        return ApiResponse.ok();
    }
}
