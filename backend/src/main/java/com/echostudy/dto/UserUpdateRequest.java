package com.echostudy.dto;

import lombok.Data;

@Data
public class UserUpdateRequest {

    private String realName;
    private String studentNo;
    private String phone;
}
