package com.echostudy.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "reservation")
public class ReservationProperties {

    private boolean onlineMaxDurationEnabled = true;
    private long onlineMaxDurationMinutes = 240;
    private long graceMinutes = 5;
    private long firstSignDeadlineMinutes = 15;
    private long banDays = 3;
}
