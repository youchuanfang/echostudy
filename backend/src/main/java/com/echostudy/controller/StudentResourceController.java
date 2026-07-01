package com.echostudy.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.echostudy.common.Result;
import com.echostudy.entity.StudyRoom;
import com.echostudy.entity.Seat;
import com.echostudy.entity.TimeNode;
import com.echostudy.enums.SpaceType;
import com.echostudy.mapper.SeatMapper;
import com.echostudy.mapper.StudyRoomMapper;
import com.echostudy.mapper.TimeNodeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/student")
public class StudentResourceController {

    private static final Set<String> SEAT_BASED_SPACE_TYPES = Set.of("STUDY_ROOM", "PUBLIC_AREA", "LAB_SEAT");

    private final StudyRoomMapper studyRoomMapper;
    private final TimeNodeMapper timeNodeMapper;
    private final SeatMapper seatMapper;

    @GetMapping("/rooms")
    public Result<List<StudyRoom>> rooms() {
        return Result.ok(studyRoomMapper.selectList(new LambdaQueryWrapper<StudyRoom>().eq(StudyRoom::getOpenStatus, true)));
    }

    @GetMapping("/spaces")
    public Result<List<StudyRoom>> spaces(@RequestParam(required = false) String building) {
        LambdaQueryWrapper<StudyRoom> query = new LambdaQueryWrapper<StudyRoom>()
                .orderByDesc(StudyRoom::getOpenStatus)
                .orderByAsc(StudyRoom::getName);
        if (hasBuildingFilter(building)) {
            query.eq(StudyRoom::getBuilding, building.trim());
        }
        return Result.ok(studyRoomMapper.selectList(query));
    }

    @GetMapping("/buildings")
    public Result<List<String>> buildings() {
        List<String> buildings = studyRoomMapper.selectList(new LambdaQueryWrapper<StudyRoom>()
                        .isNotNull(StudyRoom::getBuilding)
                        .ne(StudyRoom::getBuilding, "")
                        .orderByAsc(StudyRoom::getBuilding))
                .stream()
                .map(StudyRoom::getBuilding)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .distinct()
                .toList();
        return Result.ok(buildings);
    }

    @GetMapping("/spaces/{id}")
    public Result<StudyRoom> space(@PathVariable Long id) {
        return Result.ok(studyRoomMapper.selectById(id));
    }

    @GetMapping("/spaces/{id}/seats")
    public Result<List<Seat>> seats(@PathVariable Long id) {
        StudyRoom room = studyRoomMapper.selectById(id);
        if (room == null || !SEAT_BASED_SPACE_TYPES.contains(room.getSpaceType())) {
            return Result.ok(List.of());
        }
        return Result.ok(seatMapper.selectList(new LambdaQueryWrapper<Seat>()
                .eq(Seat::getRoomId, id)
                .orderByAsc(Seat::getRowNo, Seat::getColNo)));
    }

    @GetMapping("/space-types")
    public Result<SpaceType[]> spaceTypes() {
        return Result.ok(SpaceType.values());
    }

    @GetMapping("/time-nodes")
    public Result<List<TimeNode>> timeNodes() {
        return Result.ok(timeNodeMapper.selectList(new LambdaQueryWrapper<TimeNode>()
                .eq(TimeNode::getEnabled, true)
                .orderByAsc(TimeNode::getSortOrder)));
    }

    private boolean hasBuildingFilter(String building) {
        return building != null && !building.isBlank() && !"ALL".equalsIgnoreCase(building.trim());
    }
}
