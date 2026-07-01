package com.echostudy.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminRegisterRequest {

    @NotBlank
    private String username;
    @NotBlank
    private String password;
    private String realName;
    private String phone;
    private Boolean canRegisterAdmin = false;
}
