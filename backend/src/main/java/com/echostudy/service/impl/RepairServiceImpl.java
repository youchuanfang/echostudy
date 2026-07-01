package com.echostudy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.echostudy.dto.AdminRepairHandleRequest;
import com.echostudy.dto.RepairRequest;
import com.echostudy.entity.Notification;
import com.echostudy.entity.OperationLog;
import com.echostudy.entity.RepairRecord;
import com.echostudy.entity.Seat;
import com.echostudy.entity.StudyRoom;
import com.echostudy.entity.User;
import com.echostudy.enums.RepairLevel;
import com.echostudy.enums.RepairStatus;
import com.echostudy.exception.BusinessException;
import com.echostudy.mapper.NotificationMapper;
import com.echostudy.mapper.OperationLogMapper;
import com.echostudy.mapper.RepairRecordMapper;
import com.echostudy.mapper.SeatMapper;
import com.echostudy.mapper.StudyRoomMapper;
import com.echostudy.mapper.UserMapper;
import com.echostudy.security.UserContext;
import com.echostudy.service.RepairService;
import com.echostudy.service.ConfigService;
import com.echostudy.vo.RepairVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RepairServiceImpl implements RepairService {

    private final RepairRecordMapper repairRecordMapper;
    private final UserMapper userMapper;
    private final StudyRoomMapper studyRoomMapper;
    private final SeatMapper seatMapper;
    private final NotificationMapper notificationMapper;
    private final OperationLogMapper operationLogMapper;
    private final ConfigService configService;

    @Override
    @Transactional
    public RepairVO create(RepairRequest request) {
        if (!configService.getBoolean("repair_enabled", true)) {
            throw new BusinessException("报修功能当前未启用");
        }
        requireRoom(request.getRoomId());
        validateRepairLevel(request);
        RepairRecord repair = new RepairRecord();
        repair.setUserId(UserContext.getUserId());
        repair.setRoomId(request.getRoomId());
        repair.setSeatId(request.getSeatId());
        repair.setRepairLevel(request.getRepairLevel());
        repair.setRepairType(request.getRepairType());
        repair.setDescription(request.getDescription().trim());
        repair.setStatus(RepairStatus.PENDING.name());
        repair.setCreateTime(LocalDateTime.now());
        repairRecordMapper.insert(repair);
        return toVO(repair);
    }

    @Override
    public List<RepairVO> myRepairs() {
        return repairRecordMapper.selectList(new LambdaQueryWrapper<RepairRecord>()
                        .eq(RepairRecord::getUserId, UserContext.getUserId())
                        .orderByDesc(RepairRecord::getCreateTime))
                .stream().map(this::toVO).toList();
    }

    @Override
    @Transactional
    public RepairVO cancel(Long id) {
        RepairRecord repair = requireRepair(id);
        if (!repair.getUserId().equals(UserContext.getUserId())) {
            throw new BusinessException(403, "只能取消自己的报修");
        }
        if (!RepairStatus.PENDING.name().equals(repair.getStatus())) {
            throw new BusinessException("只有待处理报修可以取消");
        }
        LocalDateTime now = LocalDateTime.now();
        repair.setStatus(RepairStatus.CANCELLED.name());
        repair.setCancelTime(now);
        repair.setUpdateTime(now);
        repairRecordMapper.updateById(repair);
        return toVO(repair);
    }

    @Override
    public List<RepairVO> adminList(String status, String repairType, String repairLevel, Long roomId,
                                    LocalDate startDate, LocalDate endDate, String keyword) {
        LambdaQueryWrapper<RepairRecord> query = new LambdaQueryWrapper<RepairRecord>()
                .eq(status != null && !status.isBlank(), RepairRecord::getStatus, status)
                .eq(repairType != null && !repairType.isBlank(), RepairRecord::getRepairType, repairType)
                .eq(repairLevel != null && !repairLevel.isBlank(), RepairRecord::getRepairLevel, repairLevel)
                .eq(roomId != null, RepairRecord::getRoomId, roomId)
                .ge(startDate != null, RepairRecord::getCreateTime, startDate == null ? null : LocalDateTime.of(startDate, LocalTime.MIN))
                .lt(endDate != null, RepairRecord::getCreateTime, endDate == null ? null : LocalDateTime.of(endDate.plusDays(1), LocalTime.MIN))
                .orderByDesc(RepairRecord::getCreateTime);
        List<RepairVO> rows = repairRecordMapper.selectList(query).stream().map(this::toVO).toList();
        if (keyword == null || keyword.isBlank()) {
            return rows;
        }
        String term = keyword.toLowerCase();
        return rows.stream().filter(row ->
                contains(row.getUsername(), term)
                        || contains(row.getRealName(), term)
                        || contains(row.getRoomName(), term)
                        || contains(row.getSeatNo(), term)
                        || contains(row.getDescription(), term)
                        || contains(row.getAdminReply(), term)
        ).toList();
    }

    @Override
    public RepairVO detail(Long id) {
        return toVO(requireRepair(id));
    }

    @Override
    @Transactional
    public RepairVO accept(Long id, AdminRepairHandleRequest request) {
        RepairRecord repair = requireRepair(id);
        requireStatus(repair, Set.of(RepairStatus.PENDING.name()), "只有待处理报修可以受理");
        LocalDateTime now = LocalDateTime.now();
        repair.setStatus(RepairStatus.ACCEPTED.name());
        repair.setAdminId(UserContext.getUserId());
        repair.setAdminReply(blankToNull(request.getAdminReply()));
        repair.setAcceptTime(now);
        repair.setUpdateTime(now);
        applyResourceLinkage(repair, request);
        repairRecordMapper.updateById(repair);
        notifyStudent(repair, "报修已受理", "您的报修已被管理员受理。");
        logAdmin("处理报修", "受理报修 #" + repair.getId(), "REPAIR", repair.getId());
        return toVO(repair);
    }

    @Override
    @Transactional
    public RepairVO process(Long id, AdminRepairHandleRequest request) {
        RepairRecord repair = requireRepair(id);
        requireStatus(repair, Set.of(RepairStatus.ACCEPTED.name()), "只有已受理报修可以标记处理中");
        LocalDateTime now = LocalDateTime.now();
        repair.setStatus(RepairStatus.PROCESSING.name());
        repair.setAdminId(UserContext.getUserId());
        if (request.getAdminReply() != null && !request.getAdminReply().isBlank()) {
            repair.setAdminReply(request.getAdminReply().trim());
        }
        repair.setProcessTime(now);
        repair.setUpdateTime(now);
        applyResourceLinkage(repair, request);
        repairRecordMapper.updateById(repair);
        notifyStudent(repair, "报修处理中", "您的报修已进入处理流程。");
        logAdmin("处理报修", "标记报修 #" + repair.getId() + " 为处理中", "REPAIR", repair.getId());
        return toVO(repair);
    }

    @Override
    @Transactional
    public RepairVO reject(Long id, AdminRepairHandleRequest request) {
        requireText(request.getAdminReply(), "驳回原因不能为空");
        RepairRecord repair = requireRepair(id);
        requireStatus(repair, Set.of(RepairStatus.PENDING.name(), RepairStatus.ACCEPTED.name()), "只有待处理或已受理报修可以驳回");
        LocalDateTime now = LocalDateTime.now();
        repair.setStatus(RepairStatus.REJECTED.name());
        repair.setAdminId(UserContext.getUserId());
        repair.setAdminReply(request.getAdminReply().trim());
        repair.setUpdateTime(now);
        repairRecordMapper.updateById(repair);
        notifyStudent(repair, "报修已驳回", "您的报修已被驳回，原因：" + request.getAdminReply().trim());
        logAdmin("处理报修", "驳回报修 #" + repair.getId(), "REPAIR", repair.getId());
        return toVO(repair);
    }

    @Override
    @Transactional
    public RepairVO finish(Long id, AdminRepairHandleRequest request) {
        requireText(request.getAdminReply(), "处理说明不能为空");
        RepairRecord repair = requireRepair(id);
        requireStatus(repair, Set.of(RepairStatus.ACCEPTED.name(), RepairStatus.PROCESSING.name()), "只有已受理或处理中报修可以完成");
        LocalDateTime now = LocalDateTime.now();
        repair.setStatus(RepairStatus.DONE.name());
        repair.setAdminId(UserContext.getUserId());
        repair.setAdminReply(request.getAdminReply().trim());
        repair.setFinishTime(now);
        repair.setUpdateTime(now);
        applyResourceLinkage(repair, request);
        repairRecordMapper.updateById(repair);
        notifyStudent(repair, "报修已完成", "您的报修已处理完成，说明：" + request.getAdminReply().trim());
        logAdmin("处理报修", "完成报修 #" + repair.getId(), "REPAIR", repair.getId());
        return toVO(repair);
    }

    private void validateRepairLevel(RepairRequest request) {
        if (RepairLevel.SEAT.name().equals(request.getRepairLevel())) {
            if (request.getSeatId() == null) {
                throw new BusinessException("座位级报修必须选择座位/工位");
            }
            Seat seat = requireSeat(request.getSeatId());
            if (!seat.getRoomId().equals(request.getRoomId())) {
                throw new BusinessException("座位/工位不属于所选空间");
            }
            return;
        }
        if (RepairLevel.SPACE.name().equals(request.getRepairLevel())) {
            request.setSeatId(null);
            return;
        }
        throw new BusinessException("报修级别不正确");
    }

    private void applyResourceLinkage(RepairRecord repair, AdminRepairHandleRequest request) {
        if (RepairLevel.SEAT.name().equals(repair.getRepairLevel()) && repair.getSeatId() != null) {
            if (Boolean.TRUE.equals(request.getMarkSeatFaulty())) {
                updateSeatFaulty(repair.getSeatId(), true);
            }
            if (Boolean.TRUE.equals(request.getRecoverSeat())) {
                updateSeatFaulty(repair.getSeatId(), false);
            }
        }
        if (RepairLevel.SPACE.name().equals(repair.getRepairLevel())) {
            if (Boolean.TRUE.equals(request.getCloseSpace())) {
                updateSpaceOpen(repair.getRoomId(), false);
            }
            if (Boolean.TRUE.equals(request.getReopenSpace())) {
                updateSpaceOpen(repair.getRoomId(), true);
            }
        }
    }

    private void updateSeatFaulty(Long seatId, boolean faulty) {
        Seat seat = new Seat();
        seat.setId(seatId);
        seat.setFaulty(faulty);
        seat.setUpdateTime(LocalDateTime.now());
        seatMapper.updateById(seat);
    }

    private void updateSpaceOpen(Long roomId, boolean open) {
        StudyRoom room = new StudyRoom();
        room.setId(roomId);
        room.setOpenStatus(open);
        room.setUpdateTime(LocalDateTime.now());
        studyRoomMapper.updateById(room);
    }

    private void notifyStudent(RepairRecord repair, String title, String content) {
        Notification notification = new Notification();
        notification.setUserId(repair.getUserId());
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType("REPAIR");
        notification.setReadStatus(false);
        notification.setRelatedType("REPAIR");
        notification.setRelatedId(repair.getId());
        notification.setCreateTime(LocalDateTime.now());
        notificationMapper.insert(notification);
    }

    private void logAdmin(String operationType, String content, String targetType, Long targetId) {
        OperationLog log = new OperationLog();
        log.setAdminId(UserContext.getUserId());
        log.setOperationType(operationType);
        log.setOperationContent(content);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setCreateTime(LocalDateTime.now());
        operationLogMapper.insert(log);
    }

    private RepairVO toVO(RepairRecord repair) {
        RepairVO vo = RepairVO.from(repair);
        User user = userMapper.selectById(repair.getUserId());
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setRealName(user.getRealName());
        }
        User admin = repair.getAdminId() == null ? null : userMapper.selectById(repair.getAdminId());
        if (admin != null) {
            vo.setAdminName(admin.getRealName() == null ? admin.getUsername() : admin.getRealName());
        }
        StudyRoom room = studyRoomMapper.selectById(repair.getRoomId());
        if (room != null) {
            vo.setRoomName(room.getName());
            vo.setSpaceType(room.getSpaceType());
        }
        Seat seat = repair.getSeatId() == null ? null : seatMapper.selectById(repair.getSeatId());
        if (seat != null) {
            vo.setSeatNo(seat.getSeatNo());
        }
        return vo;
    }

    private RepairRecord requireRepair(Long id) {
        RepairRecord repair = repairRecordMapper.selectById(id);
        if (repair == null) {
            throw new BusinessException("报修记录不存在");
        }
        return repair;
    }

    private StudyRoom requireRoom(Long id) {
        StudyRoom room = studyRoomMapper.selectById(id);
        if (room == null) {
            throw new BusinessException("空间不存在");
        }
        return room;
    }

    private Seat requireSeat(Long id) {
        Seat seat = seatMapper.selectById(id);
        if (seat == null) {
            throw new BusinessException("座位/工位不存在");
        }
        return seat;
    }

    private void requireStatus(RepairRecord repair, Set<String> allowed, String message) {
        if (!allowed.contains(repair.getStatus())) {
            throw new BusinessException(message);
        }
    }

    private void requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new BusinessException(message);
        }
    }

    private String blankToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private boolean contains(String value, String term) {
        return value != null && value.toLowerCase().contains(term);
    }
}
