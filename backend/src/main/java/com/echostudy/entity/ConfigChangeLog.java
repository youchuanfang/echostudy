package com.echostudy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("config_change_log")
public class ConfigChangeLog {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String configKey;
    private String oldValue;
    private String newValue;
    private Long adminId;
    private LocalDateTime createTime;
}
