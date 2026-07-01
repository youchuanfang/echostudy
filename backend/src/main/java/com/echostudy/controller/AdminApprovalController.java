package com.echostudy.controller;

import com.echostudy.common.Result;
import com.echostudy.dto.ApprovalRejectRequest;
import com.echostudy.service.ReservationService;
import com.echostudy.vo.ReservationVO;
import jakarta.validation.Valid;
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
@RequestMapping("/api/admin/approvals")
public class AdminApprovalController {

    private final ReservationService reservationService;

    @GetMapping
    public Result<List<ReservationVO>> list(@RequestParam(required = false) String status,
                                            @RequestParam(required = false) String spaceType,
                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                            @RequestParam(required = false) String applicant) {
        return Result.ok(reservationService.pendingApprovals(status, spaceType, date, applicant));
    }

    @GetMapping("/{id}")
    public Result<ReservationVO> detail(@PathVariable Long id) {
        return Result.ok(reservationService.approvalDetail(id));
    }

    @PostMapping("/{id}/approve")
    public Result<ReservationVO> approve(@PathVariable Long id) {
        return Result.ok(reservationService.approve(id));
    }

    @PostMapping("/{id}/reject")
    public Result<ReservationVO> reject(@PathVariable Long id, @Valid @RequestBody ApprovalRejectRequest request) {
        return Result.ok(reservationService.reject(id, request.getRejectReason()));
    }
}
