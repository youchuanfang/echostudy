package com.echostudy.service;

import com.echostudy.entity.Reservation;
import com.echostudy.dto.ViolationAppealRequest;
import com.echostudy.dto.ViolationAppealReviewRequest;
import com.echostudy.vo.ViolationAppealVO;
import com.echostudy.vo.ViolationRecordVO;

import java.util.List;

public interface ViolationService {

    void markViolation(Reservation reservation, String violationType, String reason);

    void restoreExpiredBans();

    List<ViolationRecordVO> myViolations();

    List<ViolationRecordVO> allViolations();

    ViolationAppealVO createAppeal(Long violationId, ViolationAppealRequest request);

    List<ViolationAppealVO> myAppeals();

    List<ViolationAppealVO> allAppeals(String status);

    ViolationAppealVO approveAppeal(Long appealId, ViolationAppealReviewRequest request);

    ViolationAppealVO rejectAppeal(Long appealId, ViolationAppealReviewRequest request);
}
