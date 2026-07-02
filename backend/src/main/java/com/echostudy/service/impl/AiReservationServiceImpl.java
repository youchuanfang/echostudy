package com.echostudy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.echostudy.dto.AiTaskRequest;
import com.echostudy.entity.AiReservationTask;
import com.echostudy.entity.Notification;
import com.echostudy.entity.Seat;
import com.echostudy.entity.StudyRoom;
import com.echostudy.enums.AiTaskStatus;
import com.echostudy.enums.ReservationSource;
import com.echostudy.exception.BusinessException;
import com.echostudy.mapper.AiReservationTaskMapper;
import com.echostudy.mapper.NotificationMapper;
import com.echostudy.mapper.SeatMapper;
import com.echostudy.mapper.StudyRoomMapper;
import com.echostudy.security.UserContext;
import com.echostudy.service.AiReservationService;
import com.echostudy.service.ConfigService;
import com.echostudy.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiReservationServiceImpl implements AiReservationService {

    private final AiReservationTaskMapper aiReservationTaskMapper;
    private final StudyRoomMapper studyRoomMapper;
    private final SeatMapper seatMapper;
    private final NotificationMapper notificationMapper;
    private final ReservationService reservationService;
    private final ConfigService configService;

    @Override
    @Transactional
    public AiReservationTask createTask(AiTaskRequest request) {
        if (!configService.getBoolean("ai_task_enabled", true)) {
            throw new BusinessException("AI 自动预约当前未启用");
        }
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new BusinessException("结束时间必须晚于开始时间");
        }
        LocalDate today = LocalDate.now();
        if (request.getTargetDate().isBefore(today)) {
            throw new BusinessException("不能创建过去日期的 AI 任务");
        }
        if (request.getTargetDate().equals(today) && request.getStartTime().isBefore(LocalDateTime.now().toLocalTime().withSecond(0).withNano(0))) {
            throw new BusinessException("开始时间不能早于当前时间");
        }
        AiReservationTask task = new AiReservationTask();
        task.setUserId(UserContext.getUserId());
        task.setTargetDate(request.getTargetDate());
        task.setStartTime(request.getStartTime());
        task.setEndTime(request.getEndTime());
        task.setPreferredRoomId(request.getPreferredRoomId());
        task.setPreferSocket(Boolean.TRUE.equals(request.getPreferSocket()));
        task.setPreferWindow(Boolean.TRUE.equals(request.getPreferWindow()));
        task.setAllowChangeRoom(Boolean.TRUE.equals(request.getAllowChangeRoom()));
        task.setStatus(AiTaskStatus.PENDING.name());
        task.setCreateTime(LocalDateTime.now());
        aiReservationTaskMapper.insert(task);
        return task;
    }

    @Override
    public List<AiReservationTask> myTasks() {
        return aiReservationTaskMapper.selectList(new LambdaQueryWrapper<AiReservationTask>()
                .eq(AiReservationTask::getUserId, UserContext.getUserId())
                .orderByDesc(AiReservationTask::getCreateTime));
    }

    @Override
    public List<AiReservationTask> allTasks() {
        return aiReservationTaskMapper.selectList(new LambdaQueryWrapper<AiReservationTask>()
                .orderByDesc(AiReservationTask::getCreateTime));
    }

    @Override
    @Transactional
    public void executePendingTasks() {
        if (!configService.getBoolean("ai_task_enabled", true)) {
            return;
        }
        List<AiReservationTask> tasks = aiReservationTaskMapper.selectList(new LambdaQueryWrapper<AiReservationTask>()
                .eq(AiReservationTask::getStatus, AiTaskStatus.PENDING.name())
                .le(AiReservationTask::getTargetDate, LocalDate.now().plusDays(7))
                .orderByAsc(AiReservationTask::getCreateTime));
        int handled = 0;
        for (AiReservationTask task : tasks) {
            executeOne(task);
            handled++;
        }
        log.info("AI reservation task checked, handled {} pending tasks", handled);
    }

    private void executeOne(AiReservationTask task) {
        try {
            if (task.getTargetDate().isBefore(LocalDate.now())) {
                fail(task, "任务日期已过期");
                return;
            }
            List<StudyRoom> rooms = findCandidateRooms(task);
            Seat scoredSeat = rooms.stream()
                    .flatMap(room -> seatMapper.selectList(new LambdaQueryWrapper<Seat>()
                            .eq(Seat::getRoomId, room.getId())
                            .eq(Seat::getEnabled, true)
                            .eq(Seat::getFaulty, false)).stream())
                    .filter(seat -> !reservationService.hasSeatConflict(seat.getId(), task.getTargetDate(), task.getStartTime(), task.getEndTime()))
                    .max(Comparator.comparingInt(seat -> score(seat, task)))
                    .orElse(null);
            if (scoredSeat == null) {
                fail(task, "没有符合偏好的空闲座位");
                return;
            }
            var reservation = reservationService.createReservation(task.getUserId(), null, scoredSeat.getRoomId(), scoredSeat.getId(),
                    task.getTargetDate(), task.getStartTime(), task.getEndTime(), ReservationSource.AI.name(), "AI 自动预约", false);
            task.setStatus(AiTaskStatus.SUCCESS.name());
            task.setExecuteTime(LocalDateTime.now());
            task.setResultReservationId(reservation.getId());
            task.setUpdateTime(LocalDateTime.now());
            aiReservationTaskMapper.updateById(task);
            createNotification(task.getUserId(), "AI 预约成功", "AI 已为您创建预约 #" + reservation.getId(),
                    "AI_TASK", "AI_TASK", task.getId());
        } catch (BusinessException ex) {
            fail(task, ex.getMessage());
        } catch (Exception ex) {
            log.warn("AI reservation task failed unexpectedly, taskId={}", task.getId(), ex);
            fail(task, "AI 自动预约暂时无法完成，请稍后重试或调整预约条件");
        }
    }

    private List<StudyRoom> findCandidateRooms(AiReservationTask task) {
        if (!Boolean.TRUE.equals(task.getAllowChangeRoom())) {
            if (task.getPreferredRoomId() == null) {
                throw new BusinessException("不允许更换自习室时必须选择偏好自习室");
            }
            return studyRoomMapper.selectList(new LambdaQueryWrapper<StudyRoom>()
                    .eq(StudyRoom::getId, task.getPreferredRoomId())
                    .eq(StudyRoom::getOpenStatus, true));
        }
        return studyRoomMapper.selectList(new LambdaQueryWrapper<StudyRoom>().eq(StudyRoom::getOpenStatus, true));
    }

    private int score(Seat seat, AiReservationTask task) {
        int score = 0;
        if (task.getPreferredRoomId() != null && task.getPreferredRoomId().equals(seat.getRoomId())) {
            score += 50;
        }
        if (Boolean.TRUE.equals(task.getPreferSocket()) && Boolean.TRUE.equals(seat.getHasSocket())) {
            score += 20;
        }
        if (Boolean.TRUE.equals(task.getPreferWindow()) && Boolean.TRUE.equals(seat.getNearWindow())) {
            score += 20;
        }
        return score;
    }

    private void fail(AiReservationTask task, String reason) {
        task.setStatus(AiTaskStatus.FAILED.name());
        task.setFailReason(reason);
        task.setExecuteTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        aiReservationTaskMapper.updateById(task);
        createNotification(task.getUserId(), "AI 预约失败", reason, "AI_TASK", "AI_TASK", task.getId());
    }

    private void createNotification(Long userId, String title, String content, String type, String relatedType, Long relatedId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content == null ? "" : content);
        notification.setType(type);
        notification.setReadStatus(false);
        notification.setRelatedType(relatedType);
        notification.setRelatedId(relatedId);
        notification.setCreateTime(LocalDateTime.now());
        notificationMapper.insert(notification);
    }
}
