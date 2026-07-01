package com.echostudy.vo;

import com.echostudy.entity.Reservation;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class ReservationVO {

    private Long id;
    private Long userId;
    private Long roomId;
    private Long seatId;
    private String roomName;
    private String spaceType;
    private Boolean needApproval;
    private String seatNo;
    private String username;
    private String realName;
    private LocalDate reserveDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String status;
    private String source;
    private String purpose;
    private Integer participantCount;
    private String contactPhone;
    private Long approveAdminId;
    private LocalDateTime approveTime;
    private String rejectReason;
    private LocalDateTime signInTime;
    private LocalDateTime leaveTime;
    private LocalDateTime returnDeadline;
    private LocalDateTime returnTime;
    private LocalDateTime finishTime;
    private LocalDateTime cancelTime;
    private LocalDateTime releaseTime;
    private String remark;

    public static ReservationVO from(Reservation reservation) {
        ReservationVO vo = new ReservationVO();
        vo.setId(reservation.getId());
        vo.setUserId(reservation.getUserId());
        vo.setRoomId(reservation.getRoomId());
        vo.setSeatId(reservation.getSeatId());
        vo.setReserveDate(reservation.getReserveDate());
        vo.setStartTime(reservation.getStartTime());
        vo.setEndTime(reservation.getEndTime());
        vo.setStatus(reservation.getStatus());
        vo.setSource(reservation.getSource());
        vo.setPurpose(reservation.getPurpose());
        vo.setParticipantCount(reservation.getParticipantCount());
        vo.setContactPhone(reservation.getContactPhone());
        vo.setApproveAdminId(reservation.getApproveAdminId());
        vo.setApproveTime(reservation.getApproveTime());
        vo.setRejectReason(reservation.getRejectReason());
        vo.setSignInTime(reservation.getSignInTime());
        vo.setLeaveTime(reservation.getLeaveTime());
        vo.setReturnDeadline(reservation.getReturnDeadline());
        vo.setReturnTime(reservation.getReturnTime());
        vo.setFinishTime(reservation.getFinishTime());
        vo.setCancelTime(reservation.getCancelTime());
        vo.setReleaseTime(reservation.getReleaseTime());
        vo.setRemark(reservation.getRemark());
        return vo;
    }
}
