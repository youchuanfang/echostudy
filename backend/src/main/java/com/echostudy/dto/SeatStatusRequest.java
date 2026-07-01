package com.echostudy.dto;

import lombok.Data;

@Data
public class SeatStatusRequest {

    private Boolean enabled;
    private Boolean faulty;
}
