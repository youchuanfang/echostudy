package com.echostudy.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.echostudy.common.Result;
import com.echostudy.entity.Announcement;
import com.echostudy.mapper.AnnouncementMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/student/announcements")
public class StudentAnnouncementController {

    private final AnnouncementMapper announcementMapper;

    @GetMapping
    public Result<List<Announcement>> list() {
        return Result.ok(announcementMapper.selectList(new LambdaQueryWrapper<Announcement>()
                .eq(Announcement::getStatus, "PUBLISHED")
                .orderByDesc(Announcement::getPinned)
                .orderByDesc(Announcement::getCreateTime)));
    }
}
