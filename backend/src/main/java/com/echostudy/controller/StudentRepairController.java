package com.echostudy.controller;

import com.echostudy.common.Result;
import com.echostudy.dto.RepairRequest;
import com.echostudy.service.RepairService;
import com.echostudy.vo.RepairVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/student/repairs")
public class StudentRepairController {

    private final RepairService repairService;

    @PostMapping
    public Result<RepairVO> create(@Valid @RequestBody RepairRequest request) {
        return Result.ok(repairService.create(request));
    }

    @GetMapping("/my")
    public Result<List<RepairVO>> myRepairs() {
        return Result.ok(repairService.myRepairs());
    }

    @PostMapping("/{id}/cancel")
    public Result<RepairVO> cancel(@PathVariable Long id) {
        return Result.ok(repairService.cancel(id));
    }
}
