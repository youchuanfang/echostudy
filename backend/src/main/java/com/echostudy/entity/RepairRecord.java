package com.echostudy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("repair_record")
public class RepairRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long roomId;
    private Long seatId;
    private String repairLevel;
    private String repairType;
    private String description;
    private String status;
    private Long adminId;
    private String adminReply;
    private LocalDateTime createTime;
    private LocalDateTime acceptTime;
    private LocalDateTime processTime;
    private LocalDateTime finishTime;
    private LocalDateTime cancelTime;
    private LocalDateTime updateTime;
}
