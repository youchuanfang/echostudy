package com.echostudy.controller;

import com.echostudy.common.Result;
import com.echostudy.dto.OfflineReservationRequest;
import com.echostudy.security.UserContext;
import com.echostudy.service.ReservationService;
import com.echostudy.vo.ReservationVO;
import com.echostudy.vo.SeatLayoutVO;
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
import java.time.LocalTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminReservationController {

    private final ReservationService reservationService;

    @GetMapping("/seats/layout")
    public Result<SeatLayoutVO> layout(@RequestParam Long roomId,
                                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
                                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime) {
        return Result.ok(reservationService.layout(roomId, date, startTime, endTime, UserContext.getUserId()));
    }

    @PostMapping("/reservations/offline")
    public Result<ReservationVO> offline(@Valid @RequestBody OfflineReservationRequest request) {
        return Result.ok(reservationService.createOffline(request));
    }

    @GetMapping("/reservations")
    public Result<List<ReservationVO>> reservations() {
        return Result.ok(reservationService.allReservations());
    }

    @PostMapping("/reservations/{id}/cancel")
    public Result<ReservationVO> cancel(@PathVariable Long id) {
        return Result.ok(reservationService.adminCancel(id));
    }

    @PostMapping("/reservations/{id}/finish")
    public Result<ReservationVO> finish(@PathVariable Long id) {
        return Result.ok(reservationService.adminFinish(id));
    }
}
