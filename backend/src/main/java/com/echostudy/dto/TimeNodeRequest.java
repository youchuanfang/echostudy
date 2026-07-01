package com.echostudy.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

@Data
public class TimeNodeRequest {

    @NotNull
    private LocalTime timeValue;
    private Boolean enabled = true;
    private Integer sortOrder = 0;
}
