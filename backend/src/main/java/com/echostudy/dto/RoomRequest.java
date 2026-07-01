package com.echostudy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
public class RoomRequest {

    @NotBlank
    private String name;
    private String spaceType = "STUDY_ROOM";
    private String locationDesc;
    private Integer capacity = 0;
    @NotNull
    private LocalTime openTime;
    @NotNull
    private LocalTime closeTime;
    private Boolean openStatus = true;
    private Boolean needApproval = false;
    private Boolean needLocationCheck = true;
    private String building;
    private String floorNo;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer allowedRadiusMeter = 50;
    private String managerName;
    private String managerPhone;
    private String description;
    private String usageNotice;
}
