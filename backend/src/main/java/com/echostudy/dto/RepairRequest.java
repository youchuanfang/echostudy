package com.echostudy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RepairRequest {

    @NotNull
    private Long roomId;
    private Long seatId;
    @NotBlank
    private String repairLevel;
    @NotBlank
    private String repairType;
    @NotBlank
    private String description;
}
