package com.echostudy.vo;

import com.echostudy.entity.User;
import com.echostudy.entity.ViolationAppeal;
import com.echostudy.entity.ViolationRecord;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ViolationRecordVO {

    private Long id;
    private Long userId;
    private String username;
    private String realName;
    private Long reservationId;
    private String violationType;
    private String reason;
    private Integer creditDeductPoints;
    private Integer violationCountSnapshot;
    private LocalDateTime banEndTimeSnapshot;
    private LocalDateTime createTime;
    private Long appealId;
    private String appealStatus;
    private String appealReason;
    private String appealReply;
    private LocalDateTime appealCreateTime;
    private LocalDateTime appealReviewTime;

    public static ViolationRecordVO from(ViolationRecord record, User user, ViolationAppeal appeal) {
        ViolationRecordVO vo = new ViolationRecordVO();
        vo.setId(record.getId());
        vo.setUserId(record.getUserId());
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setRealName(user.getRealName());
        }
        vo.setReservationId(record.getReservationId());
        vo.setViolationType(record.getViolationType());
        vo.setReason(record.getReason());
        vo.setCreditDeductPoints(record.getCreditDeductPoints());
        vo.setViolationCountSnapshot(record.getViolationCountSnapshot());
        vo.setBanEndTimeSnapshot(record.getBanEndTimeSnapshot());
        vo.setCreateTime(record.getCreateTime());
        if (appeal != null) {
            vo.setAppealId(appeal.getId());
            vo.setAppealStatus(appeal.getStatus());
            vo.setAppealReason(appeal.getReason());
            vo.setAppealReply(appeal.getReviewReply());
            vo.setAppealCreateTime(appeal.getCreateTime());
            vo.setAppealReviewTime(appeal.getReviewTime());
        }
        return vo;
    }
}
