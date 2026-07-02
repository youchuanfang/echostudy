package com.echostudy.controller;

import com.echostudy.common.Result;
import com.echostudy.dto.ViolationAppealRequest;
import com.echostudy.dto.ViolationAppealReviewRequest;
import com.echostudy.service.ViolationService;
import com.echostudy.vo.ViolationAppealVO;
import com.echostudy.vo.ViolationRecordVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ViolationController {

    private final ViolationService violationService;

    @GetMapping("/api/student/violations/my")
    public Result<List<ViolationRecordVO>> myViolations() {
        return Result.ok(violationService.myViolations());
    }

    @PostMapping("/api/student/violations/{id}/appeal")
    public Result<ViolationAppealVO> createAppeal(@PathVariable Long id, @Valid @RequestBody ViolationAppealRequest request) {
        return Result.ok(violationService.createAppeal(id, request));
    }

    @GetMapping("/api/student/violation-appeals/my")
    public Result<List<ViolationAppealVO>> myAppeals() {
        return Result.ok(violationService.myAppeals());
    }

    @GetMapping("/api/admin/violations")
    public Result<List<ViolationRecordVO>> allViolations() {
        return Result.ok(violationService.allViolations());
    }

    @GetMapping("/api/admin/violation-appeals")
    public Result<List<ViolationAppealVO>> allAppeals(@RequestParam(required = false) String status) {
        return Result.ok(violationService.allAppeals(status));
    }

    @PostMapping("/api/admin/violation-appeals/{id}/approve")
    public Result<ViolationAppealVO> approveAppeal(@PathVariable Long id, @RequestBody ViolationAppealReviewRequest request) {
        return Result.ok(violationService.approveAppeal(id, request));
    }

    @PostMapping("/api/admin/violation-appeals/{id}/reject")
    public Result<ViolationAppealVO> rejectAppeal(@PathVariable Long id, @Valid @RequestBody ViolationAppealReviewRequest request) {
        return Result.ok(violationService.rejectAppeal(id, request));
    }
}
