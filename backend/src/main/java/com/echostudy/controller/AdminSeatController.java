package com.echostudy.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.echostudy.common.Result;
import com.echostudy.dto.BatchGenerateSeatRequest;
import com.echostudy.dto.SeatRequest;
import com.echostudy.dto.SeatStatusRequest;
import com.echostudy.entity.Seat;
import com.echostudy.entity.StudyRoom;
import com.echostudy.exception.BusinessException;
import com.echostudy.mapper.SeatMapper;
import com.echostudy.mapper.StudyRoomMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/seats")
public class AdminSeatController {

    private static final Set<String> SEAT_BASED_SPACE_TYPES = Set.of("STUDY_ROOM", "PUBLIC_AREA", "LAB_SEAT");

    private final SeatMapper seatMapper;
    private final StudyRoomMapper studyRoomMapper;

    @GetMapping
    public Result<List<Seat>> list(@RequestParam Long roomId) {
        if (!isSeatBasedRoom(roomId)) {
            return Result.ok(List.of());
        }
        return Result.ok(seatMapper.selectList(new LambdaQueryWrapper<Seat>()
                .eq(Seat::getRoomId, roomId)
                .orderByAsc(Seat::getRowNo, Seat::getColNo)));
    }

    @PostMapping
    public Result<Seat> create(@Valid @RequestBody SeatRequest request) {
        requireSeatBasedRoom(request.getRoomId());
        Seat seat = toEntity(request);
        seat.setCreateTime(LocalDateTime.now());
        seatMapper.insert(seat);
        refreshCapacity(request.getRoomId());
        return Result.ok(seat);
    }

    @PutMapping("/{id}")
    public Result<Seat> update(@PathVariable Long id, @Valid @RequestBody SeatRequest request) {
        requireSeatBasedRoom(request.getRoomId());
        Seat seat = toEntity(request);
        seat.setId(id);
        seat.setUpdateTime(LocalDateTime.now());
        seatMapper.updateById(seat);
        refreshCapacity(request.getRoomId());
        return Result.ok(seatMapper.selectById(id));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Seat seat = seatMapper.selectById(id);
        seatMapper.deleteById(id);
        if (seat != null) {
            refreshCapacity(seat.getRoomId());
        }
        return Result.ok();
    }

    @PutMapping("/{id}/status")
    public Result<Seat> updateStatus(@PathVariable Long id, @RequestBody SeatStatusRequest request) {
        Seat seat = new Seat();
        seat.setId(id);
        seat.setEnabled(request.getEnabled());
        seat.setFaulty(request.getFaulty());
        seat.setHasSocket(request.getHasSocket());
        seat.setNearWindow(request.getNearWindow());
        seat.setUpdateTime(LocalDateTime.now());
        seatMapper.updateById(seat);
        return Result.ok(seatMapper.selectById(id));
    }

    @PostMapping("/batch-generate")
    @Transactional
    public Result<Integer> batchGenerate(@Valid @RequestBody BatchGenerateSeatRequest request) {
        requireSeatBasedRoom(request.getRoomId());
        if (request.getRows() <= 0 || request.getCols() <= 0) {
            throw new BusinessException("行数和列数必须大于 0");
        }
        List<Seat> existingSeats = seatMapper.selectList(new LambdaQueryWrapper<Seat>()
                .eq(Seat::getRoomId, request.getRoomId()));
        for (Seat existing : existingSeats) {
            Seat update = new Seat();
            update.setId(existing.getId());
            update.setSeatNo("__TMP_" + existing.getId());
            update.setUpdateTime(LocalDateTime.now());
            seatMapper.updateById(update);
        }

        Map<String, Seat> seatsByGrid = new HashMap<>();
        for (Seat seat : existingSeats) {
            if (seat.getRowNo() <= request.getRows() && seat.getColNo() <= request.getCols()) {
                seatsByGrid.put(gridKey(seat.getRowNo(), seat.getColNo()), seat);
            } else {
                seatMapper.deleteById(seat.getId());
            }
        }

        int total = 0;
        for (int row = 1; row <= request.getRows(); row++) {
            for (int col = 1; col <= request.getCols(); col++) {
                Seat seat = seatsByGrid.get(gridKey(row, col));
                boolean exists = seat != null;
                if (!exists) {
                    seat = new Seat();
                    seat.setRoomId(request.getRoomId());
                    seat.setCreateTime(LocalDateTime.now());
                }
                seat.setSeatNo(seatNo(row, col, request.getCols()));
                seat.setRowNo(row);
                seat.setColNo(col);
                seat.setHasSocket(hasSocket(col, request.getCols()));
                seat.setNearWindow(nearWindow(row));
                seat.setEnabled(true);
                seat.setFaulty(false);
                seat.setUpdateTime(LocalDateTime.now());
                if (exists) {
                    seatMapper.updateById(seat);
                } else {
                    seatMapper.insert(seat);
                }
                total++;
            }
        }
        refreshCapacity(request.getRoomId());
        return Result.ok(total);
    }

    private String gridKey(Integer row, Integer col) {
        return row + ":" + col;
    }

    private String seatNo(int row, int col, int cols) {
        return "S" + String.format("%02d", (row - 1) * cols + col);
    }

    private boolean hasSocket(int col, int cols) {
        return col == 1 || col == cols;
    }

    private boolean nearWindow(int row) {
        return row == 1;
    }

    private Seat toEntity(SeatRequest request) {
        Seat seat = new Seat();
        seat.setRoomId(request.getRoomId());
        seat.setSeatNo(request.getSeatNo());
        seat.setRowNo(request.getRowNo());
        seat.setColNo(request.getColNo());
        seat.setHasSocket(request.getHasSocket());
        seat.setNearWindow(request.getNearWindow());
        seat.setEnabled(request.getEnabled());
        seat.setFaulty(request.getFaulty());
        seat.setRemark(request.getRemark());
        return seat;
    }

    private void refreshCapacity(Long roomId) {
        StudyRoom room = new StudyRoom();
        room.setId(roomId);
        room.setCapacity(Math.toIntExact(seatMapper.selectCount(new LambdaQueryWrapper<Seat>().eq(Seat::getRoomId, roomId))));
        room.setUpdateTime(LocalDateTime.now());
        studyRoomMapper.updateById(room);
    }

    private boolean isSeatBasedRoom(Long roomId) {
        StudyRoom room = studyRoomMapper.selectById(roomId);
        return room != null && SEAT_BASED_SPACE_TYPES.contains(room.getSpaceType());
    }

    private void requireSeatBasedRoom(Long roomId) {
        if (!isSeatBasedRoom(roomId)) {
            throw new BusinessException("该空间按整间预约，不维护座位/工位");
        }
    }
}
