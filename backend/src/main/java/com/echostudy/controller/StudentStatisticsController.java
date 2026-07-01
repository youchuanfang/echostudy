package com.echostudy.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.echostudy.common.Result;
import com.echostudy.entity.LeaveRecord;
import com.echostudy.entity.Reservation;
import com.echostudy.entity.StudyRoom;
import com.echostudy.entity.ViolationRecord;
import com.echostudy.enums.ReservationStatus;
import com.echostudy.mapper.LeaveRecordMapper;
import com.echostudy.mapper.ReservationMapper;
import com.echostudy.mapper.StudyRoomMapper;
import com.echostudy.mapper.ViolationRecordMapper;
import com.echostudy.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/student/statistics")
public class StudentStatisticsController {

    private final ReservationMapper reservationMapper;
    private final LeaveRecordMapper leaveRecordMapper;
    private final StudyRoomMapper studyRoomMapper;
    private final ViolationRecordMapper violationRecordMapper;

    @GetMapping("/learning")
    public Result<Map<String, Object>> learning() {
        Long userId = UserContext.getUserId();
        List<Reservation> reservations = reservationMapper.selectList(new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getUserId, userId)
                .orderByDesc(Reservation::getReserveDate, Reservation::getStartTime));
        List<Reservation> completed = reservations.stream()
                .filter(r -> ReservationStatus.COMPLETED.name().equals(r.getStatus()))
                .filter(r -> r.getSignInTime() != null && r.getFinishTime() != null)
                .toList();
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate monthStart = today.withDayOfMonth(1);

        Map<String, Object> data = new HashMap<>();
        data.put("todayMinutes", sumMinutes(completed, today, today));
        data.put("weekMinutes", sumMinutes(completed, weekStart, today));
        data.put("monthMinutes", sumMinutes(completed, monthStart, today));
        data.put("totalMinutes", sumMinutes(completed, null, null));
        data.put("reservationCount", reservations.size());
        data.put("signInCount", reservations.stream().filter(r -> r.getSignInTime() != null).count());
        data.put("completedCount", completed.size());
        data.put("violationCount", violationRecordMapper.selectCount(new LambdaQueryWrapper<ViolationRecord>().eq(ViolationRecord::getUserId, userId)));
        data.put("commonSpaces", topSpaces(reservations));
        data.put("commonTimeSlots", topTimeSlots(reservations));
        data.put("recentRecords", completed.stream().limit(10).map(this::recordRow).toList());
        return Result.ok(data);
    }

    private long sumMinutes(List<Reservation> reservations, LocalDate start, LocalDate end) {
        return reservations.stream()
                .filter(r -> start == null || !r.getReserveDate().isBefore(start))
                .filter(r -> end == null || !r.getReserveDate().isAfter(end))
                .mapToLong(this::actualMinutes)
                .sum();
    }

    private long actualMinutes(Reservation reservation) {
        long base = Duration.between(reservation.getSignInTime(), reservation.getFinishTime()).toMinutes();
        long leave = leaveRecordMapper.selectList(new LambdaQueryWrapper<LeaveRecord>()
                        .eq(LeaveRecord::getReservationId, reservation.getId())
                        .eq(LeaveRecord::getStatus, "RETURNED"))
                .stream()
                .filter(r -> r.getReturnTime() != null)
                .mapToLong(r -> Duration.between(r.getLeaveTime(), r.getReturnTime()).toMinutes())
                .sum();
        return Math.max(0, base - leave);
    }

    private List<Map<String, Object>> topSpaces(List<Reservation> reservations) {
        return reservations.stream().collect(Collectors.groupingBy(Reservation::getRoomId, Collectors.counting()))
                .entrySet().stream().sorted(Map.Entry.<Long, Long>comparingByValue().reversed()).limit(5)
                .map(e -> {
                    StudyRoom room = studyRoomMapper.selectById(e.getKey());
                    Map<String, Object> row = new HashMap<>();
                    row.put("roomName", room == null ? e.getKey() : room.getName());
                    row.put("count", e.getValue());
                    return row;
                }).toList();
    }

    private List<Map<String, Object>> topTimeSlots(List<Reservation> reservations) {
        return reservations.stream().collect(Collectors.groupingBy(r -> r.getStartTime() + "-" + r.getEndTime(), Collectors.counting()))
                .entrySet().stream().sorted(Map.Entry.<String, Long>comparingByValue().reversed()).limit(5)
                .map(e -> {
                    Map<String, Object> row = new HashMap<>();
                    row.put("timeSlot", e.getKey());
                    row.put("count", e.getValue());
                    return row;
                }).toList();
    }

    private Map<String, Object> recordRow(Reservation reservation) {
        StudyRoom room = studyRoomMapper.selectById(reservation.getRoomId());
        Map<String, Object> row = new HashMap<>();
        row.put("reserveDate", reservation.getReserveDate());
        row.put("roomName", room == null ? "-" : room.getName());
        row.put("startTime", reservation.getStartTime());
        row.put("endTime", reservation.getEndTime());
        row.put("minutes", actualMinutes(reservation));
        return row;
    }
}
