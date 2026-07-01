package com.echostudy.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SeatLayoutVO {

    private Long roomId;
    private String roomName;
    private Integer maxRow;
    private Integer maxCol;
    private List<SeatItem> seats = new ArrayList<>();

    @Data
    public static class SeatItem {
        private Long seatId;
        private String seatNo;
        private Integer rowNo;
        private Integer colNo;
        private Boolean hasSocket;
        private Boolean nearWindow;
        private Boolean enabled;
        private Boolean faulty;
        private String displayStatus;
        private Boolean myReservation;
        private Boolean clickable;
    }
}
