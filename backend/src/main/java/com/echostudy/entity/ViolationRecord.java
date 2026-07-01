package com.echostudy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("violation_record")
public class ViolationRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long reservationId;
    private String violationType;
    private String reason;
    private Integer violationCountSnapshot;
    private LocalDateTime banEndTimeSnapshot;
    private LocalDateTime createTime;
}
