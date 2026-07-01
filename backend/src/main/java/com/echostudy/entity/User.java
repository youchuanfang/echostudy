package com.echostudy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("users")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String realName;
    private String studentNo;
    private String phone;
    private String role;
    private String status;
    private Integer violationCount;
    private LocalDateTime banEndTime;
    private Boolean canRegisterAdmin;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
