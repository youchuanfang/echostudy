package com.echostudy.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.echostudy.common.Result;
import com.echostudy.dto.RoomRequest;
import com.echostudy.entity.OperationLog;
import com.echostudy.entity.StudyRoom;
import com.echostudy.mapper.OperationLogMapper;
import com.echostudy.mapper.StudyRoomMapper;
import com.echostudy.security.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/spaces")
public class AdminSpaceController {

    private final StudyRoomMapper studyRoomMapper;
    private final OperationLogMapper operationLogMapper;

    @GetMapping
    public Result<List<StudyRoom>> list(@RequestParam(required = false) String spaceType,
                                        @RequestParam(required = false) String building) {
        LambdaQueryWrapper<StudyRoom> query = new LambdaQueryWrapper<StudyRoom>()
                .orderByDesc(StudyRoom::getCreateTime);
        if (spaceType != null && !spaceType.isBlank()) {
            query.eq(StudyRoom::getSpaceType, spaceType);
        }
        if (hasBuildingFilter(building)) {
            query.eq(StudyRoom::getBuilding, building.trim());
        }
        return Result.ok(studyRoomMapper.selectList(query));
    }

    @GetMapping("/{id}")
    public Result<StudyRoom> detail(@PathVariable Long id) {
        return Result.ok(studyRoomMapper.selectById(id));
    }

    @PostMapping
    public Result<StudyRoom> create(@Valid @RequestBody RoomRequest request) {
        StudyRoom room = toEntity(request);
        room.setCreateTime(LocalDateTime.now());
        studyRoomMapper.insert(room);
        log("新增空间", "新增空间：" + room.getName(), "SPACE", room.getId());
        return Result.ok(room);
    }

    @PutMapping("/{id}")
    public Result<StudyRoom> update(@PathVariable Long id, @Valid @RequestBody RoomRequest request) {
        StudyRoom room = toEntity(request);
        room.setId(id);
        room.setUpdateTime(LocalDateTime.now());
        studyRoomMapper.updateById(room);
        log("修改空间", "修改空间 #" + id, "SPACE", id);
        return Result.ok(studyRoomMapper.selectById(id));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        studyRoomMapper.deleteById(id);
        log("删除空间", "删除空间 #" + id, "SPACE", id);
        return Result.ok();
    }

    @PostMapping("/{id}/open")
    public Result<StudyRoom> open(@PathVariable Long id) {
        return setOpenStatus(id, true);
    }

    @PostMapping("/{id}/close")
    public Result<StudyRoom> close(@PathVariable Long id) {
        return setOpenStatus(id, false);
    }

    private Result<StudyRoom> setOpenStatus(Long id, boolean openStatus) {
        StudyRoom room = new StudyRoom();
        room.setId(id);
        room.setOpenStatus(openStatus);
        room.setUpdateTime(LocalDateTime.now());
        studyRoomMapper.updateById(room);
        log(openStatus ? "开放空间" : "关闭空间", (openStatus ? "开放空间 #" : "关闭空间 #") + id, "SPACE", id);
        return Result.ok(studyRoomMapper.selectById(id));
    }

    private void log(String operationType, String content, String targetType, Long targetId) {
        OperationLog log = new OperationLog();
        log.setAdminId(UserContext.getUserId());
        log.setOperationType(operationType);
        log.setOperationContent(content);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setCreateTime(LocalDateTime.now());
        operationLogMapper.insert(log);
    }

    private boolean hasBuildingFilter(String building) {
        return building != null && !building.isBlank() && !"ALL".equalsIgnoreCase(building.trim());
    }

    private StudyRoom toEntity(RoomRequest request) {
        StudyRoom room = new StudyRoom();
        room.setName(request.getName());
        room.setSpaceType(request.getSpaceType());
        room.setBuilding(request.getBuilding());
        room.setFloorNo(request.getFloorNo());
        room.setLocationDesc(request.getLocationDesc());
        room.setCapacity(request.getCapacity());
        room.setOpenTime(request.getOpenTime());
        room.setCloseTime(request.getCloseTime());
        room.setOpenStatus(request.getOpenStatus());
        room.setNeedApproval(request.getNeedApproval());
        room.setNeedLocationCheck(request.getNeedLocationCheck());
        room.setLatitude(request.getLatitude());
        room.setLongitude(request.getLongitude());
        room.setAllowedRadiusMeter(request.getAllowedRadiusMeter());
        room.setManagerName(request.getManagerName());
        room.setManagerPhone(request.getManagerPhone());
        room.setUsageNotice(request.getUsageNotice());
        room.setDescription(request.getDescription());
        return room;
    }
}
