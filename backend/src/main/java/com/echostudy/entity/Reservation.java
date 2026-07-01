package com.echostudy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("reservation")
public class Reservation {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long operatorAdminId;
    private Long roomId;
    private Long seatId;
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
    private BigDecimal signInLatitude;
    private BigDecimal signInLongitude;
    private LocalDateTime leaveTime;
    private LocalDateTime returnDeadline;
    private LocalDateTime returnTime;
    private LocalDateTime finishTime;
    private LocalDateTime cancelTime;
    private LocalDateTime releaseTime;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
