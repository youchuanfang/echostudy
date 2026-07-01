package com.echostudy.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ApprovalRejectRequest {

    @NotBlank
    private String rejectReason;
}
