package com.echostudy.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.echostudy.common.Result;
import com.echostudy.entity.AiReservationTask;
import com.echostudy.entity.RepairRecord;
import com.echostudy.entity.Reservation;
import com.echostudy.entity.Seat;
import com.echostudy.entity.StudyRoom;
import com.echostudy.entity.User;
import com.echostudy.entity.ViolationRecord;
import com.echostudy.mapper.AiReservationTaskMapper;
import com.echostudy.mapper.RepairRecordMapper;
import com.echostudy.mapper.ReservationMapper;
import com.echostudy.mapper.SeatMapper;
import com.echostudy.mapper.StudyRoomMapper;
import com.echostudy.mapper.UserMapper;
import com.echostudy.mapper.ViolationRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/statistics")
public class AdminStatisticsController {

    private final StudyRoomMapper studyRoomMapper;
    private final SeatMapper seatMapper;
    private final ReservationMapper reservationMapper;
    private final RepairRecordMapper repairRecordMapper;
    private final ViolationRecordMapper violationRecordMapper;
    private final UserMapper userMapper;
    private final AiReservationTaskMapper aiReservationTaskMapper;

    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        LocalDate today = LocalDate.now();
        long reservationTotal = reservationMapper.selectCount(null);
        long approvalTotal = reservationMapper.selectCount(new LambdaQueryWrapper<Reservation>().in(Reservation::getStatus, "RESERVED", "REJECTED"));
        long approvalPass = reservationMapper.selectCount(new LambdaQueryWrapper<Reservation>().isNotNull(Reservation::getApproveTime).eq(Reservation::getStatus, "RESERVED"));
        long approvalReject = reservationMapper.selectCount(new LambdaQueryWrapper<Reservation>().eq(Reservation::getStatus, "REJECTED"));
        long repairTotal = repairRecordMapper.selectCount(null);
        long repairDone = repairRecordMapper.selectCount(new LambdaQueryWrapper<RepairRecord>().eq(RepairRecord::getStatus, "DONE"));
        long aiTotal = aiReservationTaskMapper.selectCount(null);
        long aiSuccess = aiReservationTaskMapper.selectCount(new LambdaQueryWrapper<AiReservationTask>().eq(AiReservationTask::getStatus, "SUCCESS"));
        Map<String, Object> data = new HashMap<>();
        data.put("spaceCount", studyRoomMapper.selectCount(null));
        data.put("seatCount", seatMapper.selectCount(null));
        data.put("reservationTotal", reservationTotal);
        data.put("todayReservationCount", reservationMapper.selectCount(new LambdaQueryWrapper<Reservation>().eq(Reservation::getReserveDate, today)));
        data.put("completedCount", reservationMapper.selectCount(new LambdaQueryWrapper<Reservation>().eq(Reservation::getStatus, "COMPLETED")));
        data.put("violationCount", violationRecordMapper.selectCount(null));
        data.put("bannedUserCount", userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getStatus, "BANNED")));
        data.put("repairTotal", repairTotal);
        data.put("pendingRepairCount", repairRecordMapper.selectCount(new LambdaQueryWrapper<RepairRecord>().in(RepairRecord::getStatus, "PENDING", "ACCEPTED", "PROCESSING")));
        data.put("repairDoneRate", repairTotal == 0 ? 0 : repairDone * 1.0 / repairTotal);
        data.put("approvalPassRate", approvalTotal == 0 ? 0 : approvalPass * 1.0 / approvalTotal);
        data.put("approvalRejectRate", approvalTotal == 0 ? 0 : approvalReject * 1.0 / approvalTotal);
        data.put("aiSuccessRate", aiTotal == 0 ? 0 : aiSuccess * 1.0 / aiTotal);
        return Result.ok(data);
    }

    @GetMapping("/spaces")
    public Result<List<Map<String, Object>>> spaces() {
        List<Reservation> reservations = reservationMapper.selectList(null);
        return Result.ok(studyRoomMapper.selectList(null).stream().map(room -> {
            long count = reservations.stream().filter(r -> r.getRoomId().equals(room.getId())).count();
            Map<String, Object> row = new HashMap<>();
            row.put("roomName", room.getName());
            row.put("capacity", room.getCapacity());
            row.put("reservationCount", count);
            row.put("rate", room.getCapacity() == null || room.getCapacity() == 0 ? 0 : count * 1.0 / room.getCapacity());
            return row;
        }).toList());
    }

    @GetMapping("/repairs")
    public Result<Map<String, Long>> repairs() {
        return Result.ok(repairRecordMapper.selectList(null).stream()
                .collect(Collectors.groupingBy(RepairRecord::getStatus, Collectors.counting())));
    }

    @GetMapping("/approvals")
    public Result<Map<String, Long>> approvals() {
        return Result.ok(reservationMapper.selectList(new LambdaQueryWrapper<Reservation>().isNotNull(Reservation::getApproveTime))
                .stream().collect(Collectors.groupingBy(Reservation::getStatus, Collectors.counting())));
    }

    @GetMapping("/learning-rank")
    public Result<List<Map<String, Object>>> learningRank() {
        return Result.ok(reservationMapper.selectList(new LambdaQueryWrapper<Reservation>().eq(Reservation::getStatus, "COMPLETED"))
                .stream().collect(Collectors.groupingBy(Reservation::getUserId, Collectors.counting()))
                .entrySet().stream().sorted(Map.Entry.<Long, Long>comparingByValue().reversed()).limit(10)
                .map(e -> {
                    User user = userMapper.selectById(e.getKey());
                    Map<String, Object> row = new HashMap<>();
                    row.put("userName", user == null ? e.getKey() : (user.getRealName() == null ? user.getUsername() : user.getRealName()));
                    row.put("completedCount", e.getValue());
                    return row;
                }).toList());
    }
}
