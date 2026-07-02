package com.echostudy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.echostudy.config.ReservationProperties;
import com.echostudy.dto.ViolationAppealRequest;
import com.echostudy.dto.ViolationAppealReviewRequest;
import com.echostudy.entity.Notification;
import com.echostudy.entity.OperationLog;
import com.echostudy.entity.Reservation;
import com.echostudy.entity.User;
import com.echostudy.entity.ViolationAppeal;
import com.echostudy.entity.ViolationRecord;
import com.echostudy.enums.ReservationStatus;
import com.echostudy.enums.UserStatus;
import com.echostudy.enums.ViolationAppealStatus;
import com.echostudy.exception.BusinessException;
import com.echostudy.mapper.NotificationMapper;
import com.echostudy.mapper.OperationLogMapper;
import com.echostudy.mapper.ReservationMapper;
import com.echostudy.mapper.UserMapper;
import com.echostudy.mapper.ViolationAppealMapper;
import com.echostudy.mapper.ViolationRecordMapper;
import com.echostudy.security.UserContext;
import com.echostudy.service.ConfigService;
import com.echostudy.service.ViolationService;
import com.echostudy.vo.ViolationAppealVO;
import com.echostudy.vo.ViolationRecordVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ViolationServiceImpl implements ViolationService {

    private static final int DEFAULT_CREDIT_SCORE = 100;

    private final ReservationMapper reservationMapper;
    private final UserMapper userMapper;
    private final ViolationRecordMapper violationRecordMapper;
    private final ViolationAppealMapper violationAppealMapper;
    private final NotificationMapper notificationMapper;
    private final OperationLogMapper operationLogMapper;
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

        User user = requireUser(reservation.getUserId());
        int count = (user.getViolationCount() == null ? 0 : user.getViolationCount()) + 1;
        int deductPoints = resolveDeductPoints(violationType);
        int creditScore = clampCredit(currentCredit(user) - deductPoints);
        user.setViolationCount(count);
        user.setCreditScore(creditScore);
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
        record.setCreditDeductPoints(deductPoints);
        record.setViolationCountSnapshot(count);
        record.setBanEndTimeSnapshot(user.getBanEndTime());
        record.setCreateTime(now);
        violationRecordMapper.insert(record);

        createNotification(user.getId(), "产生违规记录",
                reason + "，本次扣除信用分 " + deductPoints + " 分，当前信用分 " + creditScore + " 分。若认为记录有误，可在违规记录中提交申诉。",
                "VIOLATION", "VIOLATION", record.getId());
        if (UserStatus.BANNED.name().equals(user.getStatus())) {
            createNotification(user.getId(), "账号已封禁",
                    "您的违规次数已达到阈值，封禁至：" + user.getBanEndTime() + "。可查看违规记录并对有异议的记录提交申诉。",
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
    public List<ViolationRecordVO> myViolations() {
        return violationRecordMapper.selectList(new LambdaQueryWrapper<ViolationRecord>()
                        .eq(ViolationRecord::getUserId, UserContext.getUserId())
                        .orderByDesc(ViolationRecord::getCreateTime))
                .stream().map(this::toRecordVO).toList();
    }

    @Override
    public List<ViolationRecordVO> allViolations() {
        return violationRecordMapper.selectList(new LambdaQueryWrapper<ViolationRecord>()
                        .orderByDesc(ViolationRecord::getCreateTime))
                .stream().map(this::toRecordVO).toList();
    }

    @Override
    @Transactional
    public ViolationAppealVO createAppeal(Long violationId, ViolationAppealRequest request) {
        if (!configService.getBoolean("violation_appeal_enabled", true)) {
            throw new BusinessException("当前暂未开放违规申诉");
        }
        ViolationRecord record = requireViolation(violationId);
        Long userId = UserContext.getUserId();
        if (!record.getUserId().equals(userId)) {
            throw new BusinessException(403, "只能申诉自己的违规记录");
        }
        Long existing = violationAppealMapper.selectCount(new LambdaQueryWrapper<ViolationAppeal>()
                .eq(ViolationAppeal::getViolationId, violationId));
        if (existing > 0) {
            throw new BusinessException("该违规记录已提交过申诉，请在申诉记录中查看处理结果");
        }
        LocalDateTime now = LocalDateTime.now();
        ViolationAppeal appeal = new ViolationAppeal();
        appeal.setViolationId(violationId);
        appeal.setUserId(userId);
        appeal.setReason(request.getReason().trim());
        appeal.setEvidence(blankToNull(request.getEvidence()));
        appeal.setStatus(ViolationAppealStatus.PENDING.name());
        appeal.setCreateTime(now);
        violationAppealMapper.insert(appeal);
        createNotification(userId, "违规申诉已提交", "您的违规申诉已提交，请等待管理员处理。", "VIOLATION", "VIOLATION_APPEAL", appeal.getId());
        return toAppealVO(appeal);
    }

    @Override
    public List<ViolationAppealVO> myAppeals() {
        return violationAppealMapper.selectList(new LambdaQueryWrapper<ViolationAppeal>()
                        .eq(ViolationAppeal::getUserId, UserContext.getUserId())
                        .orderByDesc(ViolationAppeal::getCreateTime))
                .stream().map(this::toAppealVO).toList();
    }

    @Override
    public List<ViolationAppealVO> allAppeals(String status) {
        LambdaQueryWrapper<ViolationAppeal> query = new LambdaQueryWrapper<ViolationAppeal>()
                .orderByDesc(ViolationAppeal::getCreateTime);
        if (status != null && !status.isBlank()) {
            query.eq(ViolationAppeal::getStatus, status.trim().toUpperCase());
        }
        return violationAppealMapper.selectList(query).stream().map(this::toAppealVO).toList();
    }

    @Override
    @Transactional
    public ViolationAppealVO approveAppeal(Long appealId, ViolationAppealReviewRequest request) {
        ViolationAppeal appeal = requirePendingAppeal(appealId);
        ViolationRecord record = requireViolation(appeal.getViolationId());
        User user = requireUser(appeal.getUserId());
        int restorePoints = record.getCreditDeductPoints() == null ? 0 : record.getCreditDeductPoints();
        int count = Math.max(0, (user.getViolationCount() == null ? 0 : user.getViolationCount()) - 1);
        user.setViolationCount(count);
        user.setCreditScore(clampCredit(currentCredit(user) + restorePoints));
        if (UserStatus.BANNED.name().equals(user.getStatus())
                && count < configService.getInt("ban_threshold", 3)) {
            user.setStatus(UserStatus.NORMAL.name());
            user.setBanEndTime(null);
        }
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        finishAppeal(appeal, ViolationAppealStatus.APPROVED.name(), request.getReviewReply());
        createNotification(user.getId(), "违规申诉已通过",
                "您的违规申诉已通过，已恢复信用分 " + restorePoints + " 分，当前信用分 " + user.getCreditScore() + " 分。",
                "VIOLATION", "VIOLATION_APPEAL", appeal.getId());
        createOperationLog("处理违规申诉", "通过违规申诉 #" + appeal.getId(), "VIOLATION_APPEAL", appeal.getId());
        return toAppealVO(appeal);
    }

    @Override
    @Transactional
    public ViolationAppealVO rejectAppeal(Long appealId, ViolationAppealReviewRequest request) {
        ViolationAppeal appeal = requirePendingAppeal(appealId);
        String reply = blankToNull(request.getReviewReply());
        if (reply == null) {
            throw new BusinessException("驳回申诉时请填写处理说明");
        }
        finishAppeal(appeal, ViolationAppealStatus.REJECTED.name(), reply);
        createNotification(appeal.getUserId(), "违规申诉未通过",
                "您的违规申诉未通过，原因：" + reply,
                "VIOLATION", "VIOLATION_APPEAL", appeal.getId());
        createOperationLog("处理违规申诉", "驳回违规申诉 #" + appeal.getId(), "VIOLATION_APPEAL", appeal.getId());
        return toAppealVO(appeal);
    }

    private void finishAppeal(ViolationAppeal appeal, String status, String reviewReply) {
        appeal.setStatus(status);
        appeal.setReviewAdminId(UserContext.getUserId());
        appeal.setReviewReply(blankToNull(reviewReply));
        appeal.setReviewTime(LocalDateTime.now());
        appeal.setUpdateTime(LocalDateTime.now());
        violationAppealMapper.updateById(appeal);
    }

    private int resolveDeductPoints(String violationType) {
        if ("LEAVE_RETURN_TIMEOUT".equals(violationType) || "AI_LEAVE_RETURN_TIMEOUT".equals(violationType)) {
            return configService.getInt("credit_leave_return_timeout_deduct", 15);
        }
        return configService.getInt("credit_first_sign_timeout_deduct", 10);
    }

    private int currentCredit(User user) {
        return user.getCreditScore() == null ? configService.getInt("credit_initial_score", DEFAULT_CREDIT_SCORE) : user.getCreditScore();
    }

    private int clampCredit(int value) {
        int min = configService.getInt("credit_min_score", 0);
        int max = configService.getInt("credit_max_score", DEFAULT_CREDIT_SCORE);
        return Math.max(min, Math.min(max, value));
    }

    private ViolationRecord requireViolation(Long id) {
        ViolationRecord record = violationRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("违规记录不存在");
        }
        return record;
    }

    private ViolationAppeal requirePendingAppeal(Long id) {
        ViolationAppeal appeal = violationAppealMapper.selectById(id);
        if (appeal == null) {
            throw new BusinessException("申诉记录不存在");
        }
        if (!ViolationAppealStatus.PENDING.name().equals(appeal.getStatus())) {
            throw new BusinessException("该申诉已处理，不能重复操作");
        }
        return appeal;
    }

    private User requireUser(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    private ViolationRecordVO toRecordVO(ViolationRecord record) {
        User user = userMapper.selectById(record.getUserId());
        ViolationAppeal appeal = violationAppealMapper.selectOne(new LambdaQueryWrapper<ViolationAppeal>()
                .eq(ViolationAppeal::getViolationId, record.getId())
                .orderByDesc(ViolationAppeal::getCreateTime)
                .last("OFFSET 0 ROWS FETCH NEXT 1 ROWS ONLY"));
        return ViolationRecordVO.from(record, user, appeal);
    }

    private ViolationAppealVO toAppealVO(ViolationAppeal appeal) {
        User user = userMapper.selectById(appeal.getUserId());
        User admin = appeal.getReviewAdminId() == null ? null : userMapper.selectById(appeal.getReviewAdminId());
        return ViolationAppealVO.from(appeal, user, admin);
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

    private void createOperationLog(String operationType, String operationContent, String targetType, Long targetId) {
        OperationLog log = new OperationLog();
        log.setAdminId(UserContext.getUserId());
        log.setOperationType(operationType);
        log.setOperationContent(operationContent);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setCreateTime(LocalDateTime.now());
        operationLogMapper.insert(log);
    }

    private String blankToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}
