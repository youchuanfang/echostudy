package com.echostudy.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordResetRequest {

    @NotBlank
    private String phone;
    @NotBlank
    private String role;
    @NotBlank
    private String code;
    @NotBlank
    private String newPassword;
}
