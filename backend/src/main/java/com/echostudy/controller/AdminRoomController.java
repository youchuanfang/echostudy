package com.echostudy.controller;

import com.echostudy.common.Result;
import com.echostudy.dto.RoomRequest;
import com.echostudy.entity.StudyRoom;
import com.echostudy.mapper.StudyRoomMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/rooms")
public class AdminRoomController {

    private final StudyRoomMapper studyRoomMapper;

    @GetMapping
    public Result<List<StudyRoom>> list() {
        return Result.ok(studyRoomMapper.selectList(null));
    }

    @PostMapping
    public Result<StudyRoom> create(@Valid @RequestBody RoomRequest request) {
        StudyRoom room = toEntity(request);
        room.setCreateTime(LocalDateTime.now());
        studyRoomMapper.insert(room);
        return Result.ok(room);
    }

    @PutMapping("/{id}")
    public Result<StudyRoom> update(@PathVariable Long id, @Valid @RequestBody RoomRequest request) {
        StudyRoom room = toEntity(request);
        room.setId(id);
        room.setUpdateTime(LocalDateTime.now());
        studyRoomMapper.updateById(room);
        return Result.ok(studyRoomMapper.selectById(id));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        studyRoomMapper.deleteById(id);
        return Result.ok();
    }

    private StudyRoom toEntity(RoomRequest request) {
        StudyRoom room = new StudyRoom();
        room.setName(request.getName());
        room.setSpaceType(request.getSpaceType());
        room.setLocationDesc(request.getLocationDesc());
        room.setCapacity(request.getCapacity());
        room.setOpenTime(request.getOpenTime());
        room.setCloseTime(request.getCloseTime());
        room.setOpenStatus(request.getOpenStatus());
        room.setNeedApproval(request.getNeedApproval());
        room.setNeedLocationCheck(request.getNeedLocationCheck());
        room.setBuilding(request.getBuilding());
        room.setFloorNo(request.getFloorNo());
        room.setLatitude(request.getLatitude());
        room.setLongitude(request.getLongitude());
        room.setAllowedRadiusMeter(request.getAllowedRadiusMeter());
        room.setManagerName(request.getManagerName());
        room.setManagerPhone(request.getManagerPhone());
        room.setDescription(request.getDescription());
        room.setUsageNotice(request.getUsageNotice());
        return room;
    }
}
