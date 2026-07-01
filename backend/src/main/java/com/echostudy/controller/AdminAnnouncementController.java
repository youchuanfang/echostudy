package com.echostudy.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.echostudy.common.Result;
import com.echostudy.dto.AnnouncementRequest;
import com.echostudy.entity.Announcement;
import com.echostudy.entity.Notification;
import com.echostudy.entity.OperationLog;
import com.echostudy.entity.User;
import com.echostudy.mapper.AnnouncementMapper;
import com.echostudy.mapper.NotificationMapper;
import com.echostudy.mapper.OperationLogMapper;
import com.echostudy.mapper.UserMapper;
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
@RequestMapping("/api/admin/announcements")
public class AdminAnnouncementController {

    private final AnnouncementMapper announcementMapper;
    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;
    private final OperationLogMapper operationLogMapper;

    @GetMapping
    public Result<List<Announcement>> list(@RequestParam(required = false) String type,
                                           @RequestParam(required = false) String status) {
        return Result.ok(announcementMapper.selectList(new LambdaQueryWrapper<Announcement>()
                .eq(type != null && !type.isBlank(), Announcement::getType, type)
                .eq(status != null && !status.isBlank(), Announcement::getStatus, status)
                .orderByDesc(Announcement::getPinned)
                .orderByDesc(Announcement::getCreateTime)));
    }

    @PostMapping
    public Result<Announcement> create(@Valid @RequestBody AnnouncementRequest request) {
        Announcement announcement = toEntity(request);
        announcement.setCreateAdminId(UserContext.getUserId());
        announcement.setCreateTime(LocalDateTime.now());
        announcementMapper.insert(announcement);
        log("新增公告", "新增公告 #" + announcement.getId(), "ANNOUNCEMENT", announcement.getId());
        return Result.ok(announcement);
    }

    @PutMapping("/{id}")
    public Result<Announcement> update(@PathVariable Long id, @Valid @RequestBody AnnouncementRequest request) {
        Announcement announcement = toEntity(request);
        announcement.setId(id);
        announcement.setUpdateTime(LocalDateTime.now());
        announcementMapper.updateById(announcement);
        log("修改公告", "修改公告 #" + id, "ANNOUNCEMENT", id);
        return Result.ok(announcementMapper.selectById(id));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        announcementMapper.deleteById(id);
        log("删除公告", "删除公告 #" + id, "ANNOUNCEMENT", id);
        return Result.ok();
    }

    @PostMapping("/{id}/publish")
    public Result<Announcement> publish(@PathVariable Long id) {
        Announcement announcement = updateStatus(id, "PUBLISHED");
        createAnnouncementNotifications(announcement);
        log("发布公告", "发布公告 #" + id, "ANNOUNCEMENT", id);
        return Result.ok(announcement);
    }

    @PostMapping("/{id}/disable")
    public Result<Announcement> disable(@PathVariable Long id) {
        Announcement announcement = updateStatus(id, "DISABLED");
        log("停用公告", "停用公告 #" + id, "ANNOUNCEMENT", id);
        return Result.ok(announcement);
    }

    @PostMapping("/{id}/pin")
    public Result<Announcement> pin(@PathVariable Long id) {
        Announcement announcement = updatePinned(id, true);
        log("置顶公告", "置顶公告 #" + id, "ANNOUNCEMENT", id);
        return Result.ok(announcement);
    }

    @PostMapping("/{id}/unpin")
    public Result<Announcement> unpin(@PathVariable Long id) {
        Announcement announcement = updatePinned(id, false);
        log("取消置顶公告", "取消置顶公告 #" + id, "ANNOUNCEMENT", id);
        return Result.ok(announcement);
    }

    private Announcement toEntity(AnnouncementRequest request) {
        Announcement announcement = new Announcement();
        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());
        announcement.setType(request.getType());
        announcement.setPinned(Boolean.TRUE.equals(request.getPinned()));
        announcement.setStatus(request.getStatus() == null || request.getStatus().isBlank() ? "DRAFT" : request.getStatus());
        return announcement;
    }

    private Announcement updateStatus(Long id, String status) {
        Announcement update = new Announcement();
        update.setId(id);
        update.setStatus(status);
        update.setUpdateTime(LocalDateTime.now());
        announcementMapper.updateById(update);
        return announcementMapper.selectById(id);
    }

    private Announcement updatePinned(Long id, boolean pinned) {
        Announcement update = new Announcement();
        update.setId(id);
        update.setPinned(pinned);
        update.setUpdateTime(LocalDateTime.now());
        announcementMapper.updateById(update);
        return announcementMapper.selectById(id);
    }

    private void createAnnouncementNotifications(Announcement announcement) {
        List<User> students = userMapper.selectList(new LambdaQueryWrapper<User>().eq(User::getRole, "STUDENT"));
        for (User student : students) {
            Notification notification = new Notification();
            notification.setUserId(student.getId());
            notification.setTitle("公告发布：" + announcement.getTitle());
            notification.setContent(announcement.getContent().length() > 500
                    ? announcement.getContent().substring(0, 500)
                    : announcement.getContent());
            notification.setType("ANNOUNCEMENT");
            notification.setReadStatus(false);
            notification.setRelatedType("ANNOUNCEMENT");
            notification.setRelatedId(announcement.getId());
            notification.setCreateTime(LocalDateTime.now());
            notificationMapper.insert(notification);
        }
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
}
