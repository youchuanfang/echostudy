package com.echostudy.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class AiTaskRequest {

    @NotNull
    private LocalDate targetDate;
    @NotNull
    private LocalTime startTime;
    @NotNull
    private LocalTime endTime;
    private Long preferredRoomId;
    private Boolean preferSocket = false;
    private Boolean preferWindow = false;
    private Boolean allowChangeRoom = true;
}
