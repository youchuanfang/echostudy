package com.echostudy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("study_room")
public class StudyRoom {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String spaceType;
    private String locationDesc;
    private Integer capacity;
    private LocalTime openTime;
    private LocalTime closeTime;
    private Boolean openStatus;
    private Boolean needApproval;
    private Boolean needLocationCheck;
    private String building;
    private String floorNo;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer allowedRadiusMeter;
    private String managerName;
    private String managerPhone;
    private String description;
    private String usageNotice;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
