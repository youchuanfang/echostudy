package com.echostudy.controller;

import com.echostudy.common.Result;
import com.echostudy.dto.OnlineReservationRequest;
import com.echostudy.dto.SignInRequest;
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
@RequestMapping("/api/student")
public class StudentReservationController {

    private final ReservationService reservationService;

    @GetMapping("/seats/layout")
    public Result<SeatLayoutVO> layout(@RequestParam Long roomId,
                                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
                                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime) {
        return Result.ok(reservationService.layout(roomId, date, startTime, endTime, UserContext.getUserId()));
    }

    @PostMapping("/reservations/online")
    public Result<ReservationVO> online(@Valid @RequestBody OnlineReservationRequest request) {
        return Result.ok(reservationService.createOnline(request));
    }

    @GetMapping("/reservations/my")
    public Result<List<ReservationVO>> my() {
        return Result.ok(reservationService.myReservations());
    }

    @PostMapping("/reservations/{id}/cancel")
    public Result<ReservationVO> cancel(@PathVariable Long id) {
        return Result.ok(reservationService.cancel(id));
    }

    @PostMapping("/reservations/{id}/sign-in")
    public Result<ReservationVO> signIn(@PathVariable Long id, @Valid @RequestBody SignInRequest request) {
        return Result.ok(reservationService.signIn(id, request));
    }

    @PostMapping("/reservations/{id}/leave")
    public Result<ReservationVO> leave(@PathVariable Long id) {
        return Result.ok(reservationService.leave(id));
    }

    @PostMapping("/reservations/{id}/return")
    public Result<ReservationVO> returnSeat(@PathVariable Long id) {
        return Result.ok(reservationService.returnSeat(id));
    }

    @PostMapping("/reservations/{id}/finish")
    public Result<ReservationVO> finish(@PathVariable Long id) {
        return Result.ok(reservationService.finish(id));
    }
}
