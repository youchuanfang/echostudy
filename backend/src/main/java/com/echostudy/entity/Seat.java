package com.echostudy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("seat")
public class Seat {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long roomId;
    private String seatNo;
    private Integer rowNo;
    private Integer colNo;
    private Boolean hasSocket;
    private Boolean nearWindow;
    private Boolean enabled;
    private Boolean faulty;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
