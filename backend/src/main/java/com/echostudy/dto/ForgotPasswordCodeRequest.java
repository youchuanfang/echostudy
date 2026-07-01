package com.echostudy.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordCodeRequest {

    @NotBlank
    private String phone;
    @NotBlank
    private String role;
}
