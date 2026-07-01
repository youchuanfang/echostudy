package com.echostudy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.echostudy.config.ReservationProperties;
import com.echostudy.dto.OfflineReservationRequest;
import com.echostudy.dto.OnlineReservationRequest;
import com.echostudy.dto.SignInRequest;
import com.echostudy.entity.Notification;
import com.echostudy.entity.OperationLog;
import com.echostudy.entity.LeaveRecord;
import com.echostudy.entity.Reservation;
import com.echostudy.entity.Seat;
import com.echostudy.entity.StudyRoom;
import com.echostudy.entity.TimeNode;
import com.echostudy.entity.User;
import com.echostudy.enums.ReservationSource;
import com.echostudy.enums.ReservationStatus;
import com.echostudy.enums.UserStatus;
import com.echostudy.exception.BusinessException;
import com.echostudy.mapper.NotificationMapper;
import com.echostudy.mapper.OperationLogMapper;
import com.echostudy.mapper.LeaveRecordMapper;
import com.echostudy.mapper.ReservationMapper;
import com.echostudy.mapper.SeatMapper;
import com.echostudy.mapper.StudyRoomMapper;
import com.echostudy.mapper.TimeNodeMapper;
import com.echostudy.mapper.UserMapper;
import com.echostudy.security.UserContext;
import com.echostudy.service.ReservationService;
import com.echostudy.service.ConfigService;
import com.echostudy.utils.LocationUtils;
import com.echostudy.vo.ReservationVO;
import com.echostudy.vo.SeatLayoutVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private static final List<String> ACTIVE_STATUS = List.of(
            ReservationStatus.RESERVED.name(),
            ReservationStatus.USING.name(),
            ReservationStatus.LEAVE.name()
    );
    private static final Set<String> SEAT_BASED_SPACE_TYPES = Set.of("STUDY_ROOM", "PUBLIC_AREA", "LAB_SEAT");
    private static final DateTimeFormatter SQL_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final ReservationMapper reservationMapper;
    private final UserMapper userMapper;
    private final StudyRoomMapper studyRoomMapper;
    private final SeatMapper seatMapper;
    private final TimeNodeMapper timeNodeMapper;
    private final NotificationMapper notificationMapper;
    private final OperationLogMapper operationLogMapper;
    private final LeaveRecordMapper leaveRecordMapper;
    private final ReservationProperties properties;
    private final ConfigService configService;

    @Override
    public SeatLayoutVO layout(Long roomId, LocalDate date, LocalTime startTime, LocalTime endTime, Long currentUserId) {
        StudyRoom room = requireRoom(roomId);
        if (!isSeatBasedSpace(room)) {
            throw new BusinessException("该空间按整间预约，不提供座位/工位布局");
        }
        List<Seat> seats = seatMapper.selectList(new LambdaQueryWrapper<Seat>()
                .eq(Seat::getRoomId, roomId)
                .orderByAsc(Seat::getRowNo, Seat::getColNo));
        List<Reservation> reservations = reservationMapper.selectList(new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getRoomId, roomId)
                .eq(Reservation::getReserveDate, date)
                .in(Reservation::getStatus, ACTIVE_STATUS)
                .apply("start_time < CAST({0} AS time)", sqlTime(endTime))
                .apply("end_time > CAST({0} AS time)", sqlTime(startTime)));
        Map<Long, Reservation> occupied = reservations.stream()
                .filter(reservation -> reservation.getSeatId() != null)
                .collect(Collectors.toMap(Reservation::getSeatId, Function.identity(), (a, b) -> a));

        SeatLayoutVO vo = new SeatLayoutVO();
        vo.setRoomId(roomId);
        vo.setRoomName(room.getName());
        vo.setMaxRow(seats.stream().map(Seat::getRowNo).max(Integer::compareTo).orElse(0));
        vo.setMaxCol(seats.stream().map(Seat::getColNo).max(Integer::compareTo).orElse(0));
        for (Seat seat : seats) {
            SeatLayoutVO.SeatItem item = new SeatLayoutVO.SeatItem();
            item.setSeatId(seat.getId());
            item.setSeatNo(seat.getSeatNo());
            item.setRowNo(seat.getRowNo());
            item.setColNo(seat.getColNo());
            item.setHasSocket(seat.getHasSocket());
            item.setNearWindow(seat.getNearWindow());
            item.setEnabled(seat.getEnabled());
            item.setFaulty(seat.getFaulty());
            Reservation reservation = occupied.get(seat.getId());
            boolean mine = reservation != null && reservation.getUserId().equals(currentUserId);
            item.setMyReservation(mine);
            item.setDisplayStatus(resolveDisplayStatus(seat, reservation, mine));
            item.setClickable("AVAILABLE".equals(item.getDisplayStatus()));
            vo.getSeats().add(item);
        }
        return vo;
    }

    @Override
    @Transactional
    public ReservationVO createOnline(OnlineReservationRequest request) {
        StudyRoom room = requireRoom(request.getRoomId());
        boolean needApproval = configService.getBoolean("approval_enabled", true) && Boolean.TRUE.equals(room.getNeedApproval());
        if (needApproval) {
            requireText(request.getPurpose(), "需要审批的空间必须填写预约用途");
            if (request.getParticipantCount() == null || request.getParticipantCount() <= 0) {
                throw new BusinessException("需要审批的空间必须填写参与人数");
            }
            requireText(request.getContactPhone(), "需要审批的空间必须填写联系电话");
        }

        Reservation reservation = createReservation(UserContext.getUserId(), null, request.getRoomId(), request.getSeatId(),
                request.getReserveDate(), request.getStartTime(), request.getEndTime(), ReservationSource.ONLINE.name(), null, true);
        reservation.setPurpose(blankToNull(request.getPurpose()));
        reservation.setParticipantCount(request.getParticipantCount());
        reservation.setContactPhone(blankToNull(request.getContactPhone()));
        if (needApproval) {
            reservation.setStatus(ReservationStatus.PENDING_APPROVAL.name());
            createNotification(reservation.getUserId(), "预约申请已提交",
                    "您提交的空间预约申请已进入待审批，请等待管理员处理。", "APPROVAL", "RESERVATION", reservation.getId());
        }
        reservation.setUpdateTime(LocalDateTime.now());
        reservationMapper.updateById(reservation);
        return toVO(reservation);
    }

    @Override
    @Transactional
    public ReservationVO createOffline(OfflineReservationRequest request) {
        Reservation reservation = createReservation(request.getUserId(), UserContext.getUserId(), request.getRoomId(), request.getSeatId(),
                request.getReserveDate(), request.getStartTime(), request.getEndTime(), ReservationSource.OFFLINE_ADMIN.name(),
                request.getRemark(), false);
        return toVO(reservation);
    }

    @Override
    public List<ReservationVO> myReservations() {
        return reservationMapper.selectList(new LambdaQueryWrapper<Reservation>()
                        .eq(Reservation::getUserId, UserContext.getUserId())
                        .orderByDesc(Reservation::getReserveDate, Reservation::getStartTime))
                .stream().map(this::toVO).toList();
    }

    @Override
    public List<ReservationVO> allReservations() {
        return reservationMapper.selectList(new LambdaQueryWrapper<Reservation>()
                        .orderByDesc(Reservation::getReserveDate, Reservation::getStartTime))
                .stream().map(this::toVO).toList();
    }

    @Override
    public List<ReservationVO> pendingApprovals(String status, String spaceType, LocalDate date, String applicant) {
        String queryStatus = status == null || status.isBlank() ? ReservationStatus.PENDING_APPROVAL.name() : status;
        List<ReservationVO> rows = reservationMapper.selectList(new LambdaQueryWrapper<Reservation>()
                        .eq(Reservation::getStatus, queryStatus)
                        .eq(date != null, Reservation::getReserveDate, date)
                        .orderByDesc(Reservation::getCreateTime))
                .stream().map(this::toVO).toList();
        return rows.stream()
                .filter(row -> spaceType == null || spaceType.isBlank() || spaceType.equals(row.getSpaceType()))
                .filter(row -> applicant == null || applicant.isBlank()
                        || containsIgnoreCase(row.getUsername(), applicant)
                        || containsIgnoreCase(row.getRealName(), applicant))
                .toList();
    }

    @Override
    public ReservationVO approvalDetail(Long id) {
        return toVO(requireReservation(id));
    }

    @Override
    @Transactional
    public ReservationVO approve(Long id) {
        requireAdmin();
        Reservation reservation = requireReservation(id);
        if (!ReservationStatus.PENDING_APPROVAL.name().equals(reservation.getStatus())) {
            throw new BusinessException("只有待审批预约可以审批通过");
        }
        if (hasResourceConflict(reservation)
                || hasUserConflict(reservation.getUserId(), reservation.getReserveDate(), reservation.getStartTime(), reservation.getEndTime())) {
            throw new BusinessException("当前时间段资源已被占用");
        }
        LocalDateTime now = LocalDateTime.now();
        reservation.setStatus(ReservationStatus.RESERVED.name());
        reservation.setApproveAdminId(UserContext.getUserId());
        reservation.setApproveTime(now);
        reservation.setRejectReason(null);
        reservation.setUpdateTime(now);
        reservationMapper.updateById(reservation);
        createNotification(reservation.getUserId(), "预约审批已通过",
                "您的空间预约申请已审批通过，请按时到场签到。", "APPROVAL", "RESERVATION", reservation.getId());
        createOperationLog("审批预约", "通过预约申请 #" + reservation.getId(), "RESERVATION", reservation.getId());
        return toVO(reservation);
    }

    @Override
    @Transactional
    public ReservationVO reject(Long id, String rejectReason) {
        requireAdmin();
        requireText(rejectReason, "驳回原因不能为空");
        Reservation reservation = requireReservation(id);
        if (!ReservationStatus.PENDING_APPROVAL.name().equals(reservation.getStatus())) {
            throw new BusinessException("只有待审批预约可以驳回");
        }
        LocalDateTime now = LocalDateTime.now();
        reservation.setStatus(ReservationStatus.REJECTED.name());
        reservation.setApproveAdminId(UserContext.getUserId());
        reservation.setApproveTime(now);
        reservation.setRejectReason(rejectReason.trim());
        reservation.setUpdateTime(now);
        reservationMapper.updateById(reservation);
        createNotification(reservation.getUserId(), "预约审批已驳回",
                "您的空间预约申请已被驳回，原因：" + rejectReason.trim(), "APPROVAL", "RESERVATION", reservation.getId());
        createOperationLog("驳回预约", "驳回预约申请 #" + reservation.getId() + "，原因：" + rejectReason.trim(),
                "RESERVATION", reservation.getId());
        return toVO(reservation);
    }

    @Override
    @Transactional
    public ReservationVO cancel(Long id) {
        Reservation reservation = requireReservation(id);
        if (!reservation.getUserId().equals(UserContext.getUserId())) {
            throw new BusinessException(403, "只能取消自己的预约");
        }
        return toVO(doCancel(reservation));
    }

    @Override
    @Transactional
    public ReservationVO adminCancel(Long id) {
        return toVO(doCancel(requireReservation(id)));
    }

    @Override
    @Transactional
    public ReservationVO signIn(Long id, SignInRequest request) {
        User user = requireNormalUser(UserContext.getUserId());
        Reservation reservation = requireReservation(id);
        if (!reservation.getUserId().equals(user.getId())) {
            throw new BusinessException(403, "只能签到自己的预约");
        }
        if (!ReservationStatus.RESERVED.name().equals(reservation.getStatus())) {
            throw new BusinessException("只有已预约状态可以签到");
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startAt = LocalDateTime.of(reservation.getReserveDate(), reservation.getStartTime());
        if (now.isBefore(startAt) || now.isAfter(startAt.plusMinutes(configService.getInt("first_sign_deadline_minutes", properties.getFirstSignDeadlineMinutes())))) {
            throw new BusinessException("当前不在签到时间范围内");
        }
        StudyRoom room = requireRoom(reservation.getRoomId());
        if (configService.getBoolean("location_check_enabled", true)
                && Boolean.TRUE.equals(room.getNeedLocationCheck()) && room.getLatitude() != null && room.getLongitude() != null) {
            double distance = LocationUtils.distanceMeters(room.getLatitude(), room.getLongitude(), request.getLatitude(), request.getLongitude());
            if (distance > room.getAllowedRadiusMeter()) {
                throw new BusinessException("当前位置不在允许签到范围内，距离约 " + Math.round(distance) + " 米");
            }
        }
        reservation.setStatus(ReservationStatus.USING.name());
        reservation.setSignInTime(now);
        reservation.setSignInLatitude(request.getLatitude());
        reservation.setSignInLongitude(request.getLongitude());
        reservation.setUpdateTime(now);
        reservationMapper.updateById(reservation);
        return toVO(reservation);
    }

    @Override
    @Transactional
    public ReservationVO leave(Long id) {
        Reservation reservation = requireOwnReservation(id);
        requireNormalUser(UserContext.getUserId());
        if (!ReservationStatus.USING.name().equals(reservation.getStatus())) {
            throw new BusinessException("只有使用中可以暂离");
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endAt = LocalDateTime.of(reservation.getReserveDate(), reservation.getEndTime());
        reservation.setStatus(ReservationStatus.LEAVE.name());
        reservation.setLeaveTime(now);
        LocalDateTime configuredDeadline = now.plusMinutes(configService.getInt("leave_max_minutes", 60));
        reservation.setReturnDeadline(configuredDeadline.isBefore(endAt) ? configuredDeadline : endAt);
        reservation.setUpdateTime(now);
        reservationMapper.updateById(reservation);
        LeaveRecord leaveRecord = new LeaveRecord();
        leaveRecord.setReservationId(reservation.getId());
        leaveRecord.setUserId(reservation.getUserId());
        leaveRecord.setLeaveTime(now);
        leaveRecord.setReturnDeadline(reservation.getReturnDeadline());
        leaveRecord.setStatus("LEAVING");
        leaveRecord.setCreateTime(now);
        leaveRecordMapper.insert(leaveRecord);
        return toVO(reservation);
    }

    @Override
    @Transactional
    public ReservationVO returnSeat(Long id) {
        Reservation reservation = requireOwnReservation(id);
        requireNormalUser(UserContext.getUserId());
        if (!ReservationStatus.LEAVE.name().equals(reservation.getStatus())) {
            throw new BusinessException("只有暂离中可以返座");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(reservation.getReturnDeadline().plusMinutes(configService.getInt("grace_minutes", properties.getGraceMinutes())))) {
            throw new BusinessException("返座已超时，请等待系统释放");
        }
        reservation.setStatus(ReservationStatus.USING.name());
        reservation.setReturnTime(now);
        reservation.setUpdateTime(now);
        reservationMapper.updateById(reservation);
        LeaveRecord leaveRecord = leaveRecordMapper.selectOne(new LambdaQueryWrapper<LeaveRecord>()
                .eq(LeaveRecord::getReservationId, reservation.getId())
                .eq(LeaveRecord::getStatus, "LEAVING")
                .orderByDesc(LeaveRecord::getLeaveTime)
                .last("OFFSET 0 ROWS FETCH NEXT 1 ROWS ONLY"));
        if (leaveRecord != null) {
            leaveRecord.setReturnTime(now);
            leaveRecord.setStatus("RETURNED");
            leaveRecordMapper.updateById(leaveRecord);
        }
        return toVO(reservation);
    }

    @Override
    @Transactional
    public ReservationVO finish(Long id) {
        Reservation reservation = requireOwnReservation(id);
        return toVO(doFinish(reservation));
    }

    @Override
    @Transactional
    public ReservationVO adminFinish(Long id) {
        return toVO(doFinish(requireReservation(id)));
    }

    @Override
    @Transactional
    public Reservation createReservation(Long userId, Long operatorAdminId, Long roomId, Long seatId, LocalDate date,
                                         LocalTime startTime, LocalTime endTime, String source, String remark, boolean requireTimeNode) {
        User user = requireNormalUser(userId);
        if (!"STUDENT".equals(user.getRole())) {
            throw new BusinessException("只能为学生创建预约");
        }
        if (!endTime.isAfter(startTime)) {
            throw new BusinessException("结束时间必须晚于开始时间");
        }
        if (date.isBefore(LocalDate.now())) {
            throw new BusinessException("不能预约过去日期");
        }
        StudyRoom room = requireRoom(roomId);
        if (!Boolean.TRUE.equals(room.getOpenStatus())) {
            throw new BusinessException("空间未开放");
        }
        if (startTime.isBefore(room.getOpenTime()) || endTime.isAfter(room.getCloseTime())) {
            throw new BusinessException("预约时间不在空间开放时间内");
        }
        boolean seatBasedSpace = isSeatBasedSpace(room);
        if (seatBasedSpace && seatId == null) {
            throw new BusinessException("该空间需要选择座位/工位");
        }
        Seat seat = seatBasedSpace ? requireSeat(seatId) : null;
        if (seatBasedSpace && !seat.getRoomId().equals(roomId)) {
            throw new BusinessException("座位不属于该空间");
        }
        if (seatBasedSpace && (!Boolean.TRUE.equals(seat.getEnabled()) || Boolean.TRUE.equals(seat.getFaulty()))) {
            throw new BusinessException("座位不可预约");
        }
        if (!seatBasedSpace) {
            seatId = null;
        }
        if (requireTimeNode) {
            requireEnabledTimeNode(startTime);
            requireEnabledTimeNode(endTime);
            if (configService.getBoolean("online_max_duration_enabled", properties.isOnlineMaxDurationEnabled())
                    && Duration.between(startTime, endTime).toMinutes() > configService.getInt("online_max_duration_minutes", properties.getOnlineMaxDurationMinutes())) {
                throw new BusinessException("超过线上最大预约时长");
            }
        }
        if (seatBasedSpace && hasSeatConflict(seatId, date, startTime, endTime)) {
            throw new BusinessException("该座位时间段已被预约");
        }
        if (!seatBasedSpace && hasRoomConflict(roomId, date, startTime, endTime)) {
            throw new BusinessException("璇ョ┖闂村綋鍓嶆椂闂存宸茶棰勭害");
        }
        if (hasUserConflict(userId, date, startTime, endTime)) {
            throw new BusinessException("该用户同一时间段已有预约");
        }
        Reservation reservation = new Reservation();
        reservation.setUserId(userId);
        reservation.setOperatorAdminId(operatorAdminId);
        reservation.setRoomId(roomId);
        reservation.setSeatId(seatId);
        reservation.setReserveDate(date);
        reservation.setStartTime(startTime);
        reservation.setEndTime(endTime);
        reservation.setStatus(ReservationStatus.RESERVED.name());
        reservation.setSource(source);
        reservation.setRemark(remark);
        reservation.setCreateTime(LocalDateTime.now());
        reservationMapper.insert(reservation);
        return reservation;
    }

    @Override
    public boolean hasUserConflict(Long userId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        return reservationMapper.selectCount(baseConflictQuery(date, startTime, endTime).eq(Reservation::getUserId, userId)) > 0;
    }

    @Override
    public boolean hasSeatConflict(Long seatId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        if (seatId == null) {
            return false;
        }
        return reservationMapper.selectCount(baseConflictQuery(date, startTime, endTime).eq(Reservation::getSeatId, seatId)) > 0;
    }

    private boolean hasRoomConflict(Long roomId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        return reservationMapper.selectCount(baseConflictQuery(date, startTime, endTime).eq(Reservation::getRoomId, roomId)) > 0;
    }

    private boolean hasResourceConflict(Reservation reservation) {
        if (reservation.getSeatId() != null) {
            return hasSeatConflict(reservation.getSeatId(), reservation.getReserveDate(), reservation.getStartTime(), reservation.getEndTime());
        }
        return hasRoomConflict(reservation.getRoomId(), reservation.getReserveDate(), reservation.getStartTime(), reservation.getEndTime());
    }

    private boolean isSeatBasedSpace(StudyRoom room) {
        return room != null && SEAT_BASED_SPACE_TYPES.contains(room.getSpaceType());
    }

    private LambdaQueryWrapper<Reservation> baseConflictQuery(LocalDate date, LocalTime startTime, LocalTime endTime) {
        return new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getReserveDate, date)
                .in(Reservation::getStatus, ACTIVE_STATUS)
                .apply("start_time < CAST({0} AS time)", sqlTime(endTime))
                .apply("end_time > CAST({0} AS time)", sqlTime(startTime));
    }

    private String resolveDisplayStatus(Seat seat, Reservation reservation, boolean mine) {
        if (Boolean.TRUE.equals(seat.getFaulty())) {
            return "FAULTY";
        }
        if (!Boolean.TRUE.equals(seat.getEnabled())) {
            return "DISABLED";
        }
        if (mine) {
            return "MINE";
        }
        if (reservation != null) {
            return reservation.getStatus();
        }
        return "AVAILABLE";
    }

    private Reservation doCancel(Reservation reservation) {
        Set<String> allowed = Set.of(ReservationStatus.RESERVED.name(), ReservationStatus.PENDING_APPROVAL.name());
        if (!allowed.contains(reservation.getStatus())) {
            throw new BusinessException("只有已预约或待审批状态可以取消");
        }
        if (!LocalDateTime.now().isBefore(LocalDateTime.of(reservation.getReserveDate(), reservation.getStartTime()))) {
            throw new BusinessException("只有预约开始前可以取消");
        }
        reservation.setStatus(ReservationStatus.CANCELLED.name());
        reservation.setCancelTime(LocalDateTime.now());
        reservation.setUpdateTime(LocalDateTime.now());
        reservationMapper.updateById(reservation);
        return reservation;
    }

    private Reservation doFinish(Reservation reservation) {
        Set<String> allowed = Set.of(ReservationStatus.USING.name(), ReservationStatus.LEAVE.name());
        if (!allowed.contains(reservation.getStatus())) {
            throw new BusinessException("只有使用中或暂离中可以结束");
        }
        reservation.setStatus(ReservationStatus.COMPLETED.name());
        reservation.setFinishTime(LocalDateTime.now());
        reservation.setUpdateTime(LocalDateTime.now());
        reservationMapper.updateById(reservation);
        return reservation;
    }

    private Reservation requireOwnReservation(Long id) {
        Reservation reservation = requireReservation(id);
        if (!reservation.getUserId().equals(UserContext.getUserId())) {
            throw new BusinessException(403, "只能操作自己的预约");
        }
        return reservation;
    }

    private Reservation requireReservation(Long id) {
        Reservation reservation = reservationMapper.selectById(id);
        if (reservation == null) {
            throw new BusinessException("预约不存在");
        }
        return reservation;
    }

    private User requireNormalUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!UserStatus.NORMAL.name().equals(user.getStatus())) {
            throw new BusinessException("用户当前状态不允许操作");
        }
        return user;
    }

    private StudyRoom requireRoom(Long roomId) {
        StudyRoom room = studyRoomMapper.selectById(roomId);
        if (room == null) {
            throw new BusinessException("空间不存在");
        }
        return room;
    }

    private Seat requireSeat(Long seatId) {
        Seat seat = seatMapper.selectById(seatId);
        if (seat == null) {
            throw new BusinessException("座位不存在");
        }
        return seat;
    }

    private void requireEnabledTimeNode(LocalTime time) {
        Long count = timeNodeMapper.selectCount(new LambdaQueryWrapper<TimeNode>()
                .apply("time_value = CAST({0} AS time)", sqlTime(time))
                .eq(TimeNode::getEnabled, true));
        if (count == 0) {
            throw new BusinessException("预约时间必须来自启用时间节点");
        }
    }

    private ReservationVO toVO(Reservation reservation) {
        ReservationVO vo = ReservationVO.from(reservation);
        StudyRoom room = studyRoomMapper.selectById(reservation.getRoomId());
        if (room != null) {
            vo.setRoomName(room.getName());
            vo.setSpaceType(room.getSpaceType());
            vo.setNeedApproval(room.getNeedApproval());
        }
        Seat seat = seatMapper.selectById(reservation.getSeatId());
        if (seat != null) {
            vo.setSeatNo(seat.getSeatNo());
        }
        User user = userMapper.selectById(reservation.getUserId());
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setRealName(user.getRealName());
        }
        return vo;
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

    private void requireAdmin() {
        if (!"ADMIN".equals(UserContext.getRole())) {
            throw new BusinessException(403, "管理员接口仅允许管理员访问");
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

    private boolean containsIgnoreCase(String text, String keyword) {
        return text != null && keyword != null && text.toLowerCase().contains(keyword.toLowerCase());
    }

    private String sqlTime(LocalTime time) {
        return time.format(SQL_TIME_FORMATTER);
    }
}
