package com.echostudy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SeatRequest {

    @NotNull
    private Long roomId;
    @NotBlank
    private String seatNo;
    @NotNull
    private Integer rowNo;
    @NotNull
    private Integer colNo;
    private Boolean hasSocket = false;
    private Boolean nearWindow = false;
    private Boolean enabled = true;
    private Boolean faulty = false;
    private String remark;
}
