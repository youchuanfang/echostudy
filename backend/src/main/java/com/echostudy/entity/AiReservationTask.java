package com.echostudy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("ai_reservation_task")
public class AiReservationTask {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private LocalDate targetDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Long preferredRoomId;
    private Boolean preferSocket;
    private Boolean preferWindow;
    private Boolean allowChangeRoom;
    private String status;
    private LocalDateTime executeTime;
    private Long resultReservationId;
    private String failReason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
