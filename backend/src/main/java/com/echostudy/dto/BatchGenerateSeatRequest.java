package com.echostudy.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BatchGenerateSeatRequest {

    @NotNull
    private Long roomId;
    @NotNull
    private Integer rows;
    @NotNull
    private Integer cols;
}
