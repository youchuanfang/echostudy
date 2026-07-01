package com.echostudy.service;

import com.echostudy.dto.OfflineReservationRequest;
import com.echostudy.dto.OnlineReservationRequest;
import com.echostudy.dto.SignInRequest;
import com.echostudy.entity.Reservation;
import com.echostudy.vo.ReservationVO;
import com.echostudy.vo.SeatLayoutVO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ReservationService {

    SeatLayoutVO layout(Long roomId, LocalDate date, LocalTime startTime, LocalTime endTime, Long currentUserId);

    ReservationVO createOnline(OnlineReservationRequest request);

    ReservationVO createOffline(OfflineReservationRequest request);

    List<ReservationVO> myReservations();

    List<ReservationVO> allReservations();

    List<ReservationVO> pendingApprovals(String status, String spaceType, LocalDate date, String applicant);

    ReservationVO approvalDetail(Long id);

    ReservationVO approve(Long id);

    ReservationVO reject(Long id, String rejectReason);

    ReservationVO cancel(Long id);

    ReservationVO adminCancel(Long id);

    ReservationVO signIn(Long id, SignInRequest request);

    ReservationVO leave(Long id);

    ReservationVO returnSeat(Long id);

    ReservationVO finish(Long id);

    ReservationVO adminFinish(Long id);

    Reservation createReservation(Long userId, Long operatorAdminId, Long roomId, Long seatId, LocalDate date,
                                  LocalTime startTime, LocalTime endTime, String source, String remark, boolean requireTimeNode);

    boolean hasUserConflict(Long userId, LocalDate date, LocalTime startTime, LocalTime endTime);

    boolean hasSeatConflict(Long seatId, LocalDate date, LocalTime startTime, LocalTime endTime);
}
