package com.echostudy.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class OfflineReservationRequest {

    @NotNull
    private Long userId;
    @NotNull
    private Long roomId;
    private Long seatId;
    @NotNull
    private LocalDate reserveDate;
    @NotNull
    private LocalTime startTime;
    @NotNull
    private LocalTime endTime;
    private String remark;
}
