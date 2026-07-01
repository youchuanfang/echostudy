package com.echostudy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.echostudy.config.ReservationProperties;
import com.echostudy.entity.Reservation;
import com.echostudy.entity.Notification;
import com.echostudy.entity.User;
import com.echostudy.entity.ViolationRecord;
import com.echostudy.enums.ReservationStatus;
import com.echostudy.enums.UserStatus;
import com.echostudy.mapper.ReservationMapper;
import com.echostudy.mapper.NotificationMapper;
import com.echostudy.mapper.UserMapper;
import com.echostudy.mapper.ViolationRecordMapper;
import com.echostudy.security.UserContext;
import com.echostudy.service.ViolationService;
import com.echostudy.service.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ViolationServiceImpl implements ViolationService {

    private final ReservationMapper reservationMapper;
    private final UserMapper userMapper;
    private final ViolationRecordMapper violationRecordMapper;
    private final NotificationMapper notificationMapper;
    private final ReservationProperties properties;
    private final ConfigService configService;

    @Override
    @Transactional
    public void markViolation(Reservation reservation, String violationType, String reason) {
        LocalDateTime now = LocalDateTime.now();
        reservation.setStatus(ReservationStatus.VIOLATED.name());
        reservation.setReleaseTime(now);
        reservation.setUpdateTime(now);
        reservationMapper.updateById(reservation);

        User user = userMapper.selectById(reservation.getUserId());
        int count = (user.getViolationCount() == null ? 0 : user.getViolationCount()) + 1;
        user.setViolationCount(count);
        if (count >= configService.getInt("ban_threshold", 3)) {
            user.setStatus(UserStatus.BANNED.name());
            user.setBanEndTime(now.plusDays(configService.getInt("ban_days", properties.getBanDays())));
        }
        user.setUpdateTime(now);
        userMapper.updateById(user);

        ViolationRecord record = new ViolationRecord();
        record.setUserId(user.getId());
        record.setReservationId(reservation.getId());
        record.setViolationType(violationType);
        record.setReason(reason);
        record.setViolationCountSnapshot(count);
        record.setBanEndTimeSnapshot(user.getBanEndTime());
        record.setCreateTime(now);
        violationRecordMapper.insert(record);
        createNotification(user.getId(), "产生违规记录", reason, "VIOLATION", "VIOLATION", record.getId());
        if (UserStatus.BANNED.name().equals(user.getStatus())) {
            createNotification(user.getId(), "账号已封禁", "您的违规次数已达到阈值，账号封禁至：" + user.getBanEndTime(),
                    "BAN", "USER", user.getId());
        }
    }

    @Override
    @Transactional
    public void restoreExpiredBans() {
        List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>()
                .eq(User::getStatus, UserStatus.BANNED.name())
                .le(User::getBanEndTime, LocalDateTime.now()));
        for (User user : users) {
            user.setStatus(UserStatus.NORMAL.name());
            user.setViolationCount(0);
            user.setBanEndTime(null);
            user.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(user);
            createNotification(user.getId(), "封禁已解除", "您的账号封禁已到期，当前状态已恢复正常。", "BAN", "USER", user.getId());
        }
    }

    @Override
    public List<ViolationRecord> myViolations() {
        return violationRecordMapper.selectList(new LambdaQueryWrapper<ViolationRecord>()
                .eq(ViolationRecord::getUserId, UserContext.getUserId())
                .orderByDesc(ViolationRecord::getCreateTime));
    }

    @Override
    public List<ViolationRecord> allViolations() {
        return violationRecordMapper.selectList(new LambdaQueryWrapper<ViolationRecord>()
                .orderByDesc(ViolationRecord::getCreateTime));
    }

    private void createNotification(Long userId, String title, String content, String type, String relatedType, Long relatedId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setReadStatus(false);
        notification.setRelatedType(relatedType);
        notification.setRelatedId(relatedId);
        notification.setCreateTime(LocalDateTime.now());
        notificationMapper.insert(notification);
    }
}
