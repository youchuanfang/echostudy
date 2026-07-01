package com.echostudy.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.echostudy.config.ReservationProperties;
import com.echostudy.entity.LeaveRecord;
import com.echostudy.entity.Reservation;
import com.echostudy.enums.ReservationSource;
import com.echostudy.enums.ReservationStatus;
import com.echostudy.enums.ViolationType;
import com.echostudy.mapper.LeaveRecordMapper;
import com.echostudy.mapper.ReservationMapper;
import com.echostudy.service.AiReservationService;
import com.echostudy.service.ConfigService;
import com.echostudy.service.ViolationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationMaintenanceTask {

    private final ReservationMapper reservationMapper;
    private final LeaveRecordMapper leaveRecordMapper;
    private final ViolationService violationService;
    private final AiReservationService aiReservationService;
    private final ReservationProperties properties;
    private final ConfigService configService;

    @Scheduled(fixedDelay = 60000)
    public void runMaintenance() {
        log.info("EchoStudy maintenance task started");
        handleFirstSignTimeout();
        handleLeaveReturnTimeout();
        handleReservationExpired();
        violationService.restoreExpiredBans();
        aiReservationService.executePendingTasks();
        log.info("EchoStudy maintenance task finished");
    }

    private void handleFirstSignTimeout() {
        LocalDateTime now = LocalDateTime.now();
        List<Reservation> reservations = reservationMapper.selectList(new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getStatus, ReservationStatus.RESERVED.name()));
        int count = 0;
        int deadlineMinutes = configService.getInt("first_sign_deadline_minutes", properties.getFirstSignDeadlineMinutes());
        for (Reservation reservation : reservations) {
            LocalDateTime deadline = LocalDateTime.of(reservation.getReserveDate(), reservation.getStartTime())
                    .plusMinutes(deadlineMinutes);
            if (now.isAfter(deadline)) {
                String type = ReservationSource.AI.name().equals(reservation.getSource())
                        ? ViolationType.AI_FIRST_SIGN_TIMEOUT.name()
                        : ViolationType.FIRST_SIGN_TIMEOUT.name();
                violationService.markViolation(reservation, type, "首次签到超时");
                count++;
            }
        }
        log.info("First sign timeout checked, released {} reservations", count);
    }

    private void handleLeaveReturnTimeout() {
        LocalDateTime now = LocalDateTime.now();
        List<Reservation> reservations = reservationMapper.selectList(new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getStatus, ReservationStatus.LEAVE.name()));
        int count = 0;
        int graceMinutes = configService.getInt("grace_minutes", properties.getGraceMinutes());
        for (Reservation reservation : reservations) {
            if (reservation.getReturnDeadline() != null
                    && now.isAfter(reservation.getReturnDeadline().plusMinutes(graceMinutes))) {
                String type = ReservationSource.AI.name().equals(reservation.getSource())
                        ? ViolationType.AI_LEAVE_RETURN_TIMEOUT.name()
                        : ViolationType.LEAVE_RETURN_TIMEOUT.name();
                markLeaveRecordTimeout(reservation);
                violationService.markViolation(reservation, type, "暂离未按时返座");
                count++;
            }
        }
        log.info("Leave return timeout checked, released {} reservations", count);
    }

    private void handleReservationExpired() {
        LocalDateTime now = LocalDateTime.now();
        List<Reservation> reservations = reservationMapper.selectList(new LambdaQueryWrapper<Reservation>()
                .in(Reservation::getStatus, ReservationStatus.USING.name(), ReservationStatus.RESERVED.name(), ReservationStatus.LEAVE.name()));
        int completed = 0;
        int graceMinutes = configService.getInt("grace_minutes", properties.getGraceMinutes());
        for (Reservation reservation : reservations) {
            LocalDateTime endWithGrace = LocalDateTime.of(reservation.getReserveDate(), reservation.getEndTime())
                    .plusMinutes(graceMinutes);
            if (!now.isAfter(endWithGrace)) {
                continue;
            }
            if (ReservationStatus.USING.name().equals(reservation.getStatus())) {
                reservation.setStatus(ReservationStatus.COMPLETED.name());
                reservation.setFinishTime(now);
                reservation.setUpdateTime(now);
                reservationMapper.updateById(reservation);
                completed++;
            } else if (ReservationStatus.LEAVE.name().equals(reservation.getStatus())) {
                String type = ReservationSource.AI.name().equals(reservation.getSource())
                        ? ViolationType.AI_LEAVE_RETURN_TIMEOUT.name()
                        : ViolationType.LEAVE_RETURN_TIMEOUT.name();
                markLeaveRecordTimeout(reservation);
                violationService.markViolation(reservation, type, "预约结束时仍处于暂离状态");
            } else if (ReservationStatus.RESERVED.name().equals(reservation.getStatus())) {
                String type = ReservationSource.AI.name().equals(reservation.getSource())
                        ? ViolationType.AI_FIRST_SIGN_TIMEOUT.name()
                        : ViolationType.FIRST_SIGN_TIMEOUT.name();
                violationService.markViolation(reservation, type, "预约结束仍未签到");
            }
        }
        log.info("Reservation expired checked, completed {} reservations", completed);
    }

    private void markLeaveRecordTimeout(Reservation reservation) {
        LeaveRecord leaveRecord = leaveRecordMapper.selectOne(new LambdaQueryWrapper<LeaveRecord>()
                .eq(LeaveRecord::getReservationId, reservation.getId())
                .eq(LeaveRecord::getStatus, "LEAVING")
                .orderByDesc(LeaveRecord::getLeaveTime)
                .last("OFFSET 0 ROWS FETCH NEXT 1 ROWS ONLY"));
        if (leaveRecord != null) {
            leaveRecord.setStatus("TIMEOUT");
            leaveRecordMapper.updateById(leaveRecord);
        }
    }
}
