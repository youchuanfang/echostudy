package com.echostudy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("violation_appeal")
public class ViolationAppeal {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long violationId;
    private Long userId;
    private String reason;
    private String evidence;
    private String status;
    private Long reviewAdminId;
    private String reviewReply;
    private LocalDateTime reviewTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
