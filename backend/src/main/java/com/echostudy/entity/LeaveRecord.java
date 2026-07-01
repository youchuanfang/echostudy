package com.echostudy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("leave_record")
public class LeaveRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long reservationId;
    private Long userId;
    private LocalDateTime leaveTime;
    private LocalDateTime returnDeadline;
    private LocalDateTime returnTime;
    private String status;
    private LocalDateTime createTime;
}
