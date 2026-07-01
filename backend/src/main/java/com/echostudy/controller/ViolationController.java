package com.echostudy.controller;

import com.echostudy.common.Result;
import com.echostudy.entity.ViolationRecord;
import com.echostudy.service.ViolationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ViolationController {

    private final ViolationService violationService;

    @GetMapping("/api/student/violations/my")
    public Result<List<ViolationRecord>> myViolations() {
        return Result.ok(violationService.myViolations());
    }

    @GetMapping("/api/admin/violations")
    public Result<List<ViolationRecord>> allViolations() {
        return Result.ok(violationService.allViolations());
    }
}
