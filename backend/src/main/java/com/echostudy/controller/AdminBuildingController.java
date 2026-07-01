package com.echostudy.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.echostudy.common.Result;
import com.echostudy.entity.StudyRoom;
import com.echostudy.mapper.StudyRoomMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminBuildingController {

    private final StudyRoomMapper studyRoomMapper;

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
}
