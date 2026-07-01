package com.echostudy.controller;

import com.echostudy.common.Result;
import com.echostudy.dto.AdminRepairHandleRequest;
import com.echostudy.service.RepairService;
import com.echostudy.vo.RepairVO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/repairs")
public class AdminRepairController {

    private final RepairService repairService;

    @GetMapping
    public Result<List<RepairVO>> list(@RequestParam(required = false) String status,
                                       @RequestParam(required = false) String repairType,
                                       @RequestParam(required = false) String repairLevel,
                                       @RequestParam(required = false) Long roomId,
                                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                       @RequestParam(required = false) String keyword) {
        return Result.ok(repairService.adminList(status, repairType, repairLevel, roomId, startDate, endDate, keyword));
    }

    @GetMapping("/{id}")
    public Result<RepairVO> detail(@PathVariable Long id) {
        return Result.ok(repairService.detail(id));
    }

    @PostMapping("/{id}/accept")
    public Result<RepairVO> accept(@PathVariable Long id, @RequestBody(required = false) AdminRepairHandleRequest request) {
        return Result.ok(repairService.accept(id, request == null ? new AdminRepairHandleRequest() : request));
    }

    @PostMapping("/{id}/process")
    public Result<RepairVO> process(@PathVariable Long id, @RequestBody(required = false) AdminRepairHandleRequest request) {
        return Result.ok(repairService.process(id, request == null ? new AdminRepairHandleRequest() : request));
    }

    @PostMapping("/{id}/reject")
    public Result<RepairVO> reject(@PathVariable Long id, @RequestBody AdminRepairHandleRequest request) {
        return Result.ok(repairService.reject(id, request));
    }

    @PostMapping("/{id}/finish")
    public Result<RepairVO> finish(@PathVariable Long id, @RequestBody AdminRepairHandleRequest request) {
        return Result.ok(repairService.finish(id, request));
    }
}
