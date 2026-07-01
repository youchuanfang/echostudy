package com.echostudy.dto;

import lombok.Data;

@Data
public class AdminRepairHandleRequest {

    private String adminReply;
    private Boolean markSeatFaulty = false;
    private Boolean recoverSeat = false;
    private Boolean closeSpace = false;
    private Boolean reopenSpace = false;
}
