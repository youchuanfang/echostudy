package com.echostudy.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.echostudy.common.Result;
import com.echostudy.entity.Notification;
import com.echostudy.entity.User;
import com.echostudy.mapper.NotificationMapper;
import com.echostudy.mapper.UserMapper;
import com.echostudy.security.UserContext;
import com.echostudy.vo.NotificationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;

    @GetMapping("/api/student/notifications")
    public Result<List<NotificationVO>> myNotifications(@RequestParam(required = false) String type) {
        return Result.ok(notificationMapper.selectList(new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, UserContext.getUserId())
                        .eq(type != null && !type.isBlank(), Notification::getType, type)
                        .orderByDesc(Notification::getCreateTime))
                .stream().map(this::toVO).toList());
    }

    @GetMapping("/api/student/notifications/unread-count")
    public Result<Long> unreadCount() {
        return Result.ok(notificationMapper.selectCount(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, UserContext.getUserId())
                .eq(Notification::getReadStatus, false)));
    }

    @PostMapping("/api/student/notifications/{id}/read")
    public Result<NotificationVO> read(@PathVariable Long id) {
        Notification notification = notificationMapper.selectById(id);
        if (notification != null && notification.getUserId().equals(UserContext.getUserId())) {
            notification.setReadStatus(true);
            notification.setReadTime(LocalDateTime.now());
            notificationMapper.updateById(notification);
        }
        return Result.ok(toVO(notificationMapper.selectById(id)));
    }

    @PostMapping("/api/student/notifications/read-all")
    public Result<Void> readAll() {
        List<Notification> notifications = notificationMapper.selectList(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, UserContext.getUserId())
                .eq(Notification::getReadStatus, false));
        for (Notification notification : notifications) {
            notification.setReadStatus(true);
            notification.setReadTime(LocalDateTime.now());
            notificationMapper.updateById(notification);
        }
        return Result.ok();
    }

    @GetMapping("/api/admin/notifications")
    public Result<List<NotificationVO>> adminNotifications(@RequestParam(required = false) Long userId,
                                                          @RequestParam(required = false) String type,
                                                          @RequestParam(required = false) Boolean readStatus,
                                                          @RequestParam(required = false) String keyword) {
        List<NotificationVO> rows = notificationMapper.selectList(new LambdaQueryWrapper<Notification>()
                        .eq(userId != null, Notification::getUserId, userId)
                        .eq(type != null && !type.isBlank(), Notification::getType, type)
                        .eq(readStatus != null, Notification::getReadStatus, readStatus)
                        .orderByDesc(Notification::getCreateTime))
                .stream().map(this::toVO).toList();
        if (keyword == null || keyword.isBlank()) {
            return Result.ok(rows);
        }
        String term = keyword.toLowerCase();
        return Result.ok(rows.stream()
                .filter(row -> contains(row.getUsername(), term)
                        || contains(row.getRealName(), term)
                        || contains(row.getTitle(), term)
                        || contains(row.getContent(), term))
                .toList());
    }

    private NotificationVO toVO(Notification notification) {
        if (notification == null) {
            return null;
        }
        NotificationVO vo = NotificationVO.from(notification);
        User user = userMapper.selectById(notification.getUserId());
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setRealName(user.getRealName());
        }
        return vo;
    }

    private boolean contains(String value, String term) {
        return value != null && value.toLowerCase().contains(term);
    }
}
