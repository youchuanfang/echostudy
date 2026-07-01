package com.echostudy.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.echostudy.common.Result;
import com.echostudy.entity.Reservation;
import com.echostudy.entity.RepairRecord;
import com.echostudy.entity.AiReservationTask;
import com.echostudy.entity.OperationLog;
import com.echostudy.entity.StudyRoom;
import com.echostudy.entity.User;
import com.echostudy.entity.ViolationRecord;
import com.echostudy.enums.ReservationStatus;
import com.echostudy.mapper.ReservationMapper;
import com.echostudy.mapper.RepairRecordMapper;
import com.echostudy.mapper.AiReservationTaskMapper;
import com.echostudy.mapper.OperationLogMapper;
import com.echostudy.mapper.StudyRoomMapper;
import com.echostudy.mapper.UserMapper;
import com.echostudy.mapper.ViolationRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminDashboardController {

    private final ReservationMapper reservationMapper;
    private final StudyRoomMapper studyRoomMapper;
    private final ViolationRecordMapper violationRecordMapper;
    private final UserMapper userMapper;
    private final RepairRecordMapper repairRecordMapper;
    private final AiReservationTaskMapper aiReservationTaskMapper;
    private final OperationLogMapper operationLogMapper;

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = LocalDateTime.of(today, LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(today.plusDays(1), LocalTime.MIN);
        Map<String, Object> data = new HashMap<>();
        List<Reservation> todayReservations = reservationMapper.selectList(new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getReserveDate, today));
        data.put("todayReservationCount", todayReservations.size());
        data.put("pendingApprovalCount", reservationMapper.selectCount(new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getStatus, ReservationStatus.PENDING_APPROVAL.name())));
        data.put("todaySignInCount", todayReservations.stream().filter(r -> r.getSignInTime() != null).count());
        data.put("todayLeaveCount", todayReservations.stream().filter(r -> r.getLeaveTime() != null).count());
        data.put("todayReleaseCount", todayReservations.stream().filter(r -> r.getReleaseTime() != null).count());
        data.put("todayRepairCount", repairRecordMapper.selectCount(new LambdaQueryWrapper<RepairRecord>().ge(RepairRecord::getCreateTime, start).lt(RepairRecord::getCreateTime, end)));
        data.put("pendingRepairCount", repairRecordMapper.selectCount(new LambdaQueryWrapper<RepairRecord>().in(RepairRecord::getStatus, "PENDING", "ACCEPTED", "PROCESSING")));
        data.put("todayViolationCount", violationRecordMapper.selectCount(new LambdaQueryWrapper<ViolationRecord>().ge(ViolationRecord::getCreateTime, start).lt(ViolationRecord::getCreateTime, end)));
        data.put("bannedUserCount", userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getStatus, "BANNED")));
        long aiTotal = aiReservationTaskMapper.selectCount(null);
        long aiSuccess = aiReservationTaskMapper.selectCount(new LambdaQueryWrapper<AiReservationTask>().eq(AiReservationTask::getStatus, "SUCCESS"));
        data.put("aiSuccessRate", aiTotal == 0 ? 0 : aiSuccess * 1.0 / aiTotal);
        data.put("roomRates", roomRates(todayReservations));
        data.put("violationRanking", violationRanking(start, end));
        data.put("latestOperationLogs", operationLogMapper.selectList(new LambdaQueryWrapper<OperationLog>().orderByDesc(OperationLog::getCreateTime).last("OFFSET 0 ROWS FETCH NEXT 5 ROWS ONLY")));
        return Result.ok(data);
    }

    private List<Map<String, Object>> roomRates(List<Reservation> todayReservations) {
        return studyRoomMapper.selectList(null).stream().map(room -> {
            long active = todayReservations.stream()
                    .filter(r -> r.getRoomId().equals(room.getId()))
                    .filter(r -> List.of(ReservationStatus.RESERVED.name(), ReservationStatus.USING.name(), ReservationStatus.LEAVE.name())
                            .contains(r.getStatus()))
                    .count();
            Map<String, Object> row = new HashMap<>();
            row.put("roomId", room.getId());
            row.put("roomName", room.getName());
            row.put("capacity", room.getCapacity());
            row.put("activeReservationCount", active);
            row.put("rate", room.getCapacity() == null || room.getCapacity() == 0 ? 0 : active * 1.0 / room.getCapacity());
            return row;
        }).toList();
    }

    private List<Map<String, Object>> violationRanking(LocalDateTime start, LocalDateTime end) {
        List<ViolationRecord> records = violationRecordMapper.selectList(new LambdaQueryWrapper<ViolationRecord>()
                .ge(ViolationRecord::getCreateTime, start)
                .lt(ViolationRecord::getCreateTime, end));
        return records.stream()
                .collect(java.util.stream.Collectors.groupingBy(ViolationRecord::getUserId, java.util.stream.Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(10)
                .map(entry -> {
                    User user = userMapper.selectById(entry.getKey());
                    Map<String, Object> row = new HashMap<>();
                    row.put("userId", entry.getKey());
                    row.put("username", user == null ? "" : user.getUsername());
                    row.put("count", entry.getValue());
                    return row;
                }).toList();
    }
}
