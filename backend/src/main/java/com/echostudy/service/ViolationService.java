package com.echostudy.service;

import com.echostudy.entity.Reservation;
import com.echostudy.entity.ViolationRecord;

import java.util.List;

public interface ViolationService {

    void markViolation(Reservation reservation, String violationType, String reason);

    void restoreExpiredBans();

    List<ViolationRecord> myViolations();

    List<ViolationRecord> allViolations();
}
