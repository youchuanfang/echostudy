package com.echostudy.vo;

import com.echostudy.entity.User;
import com.echostudy.entity.ViolationAppeal;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ViolationAppealVO {

    private Long id;
    private Long violationId;
    private Long userId;
    private String username;
    private String realName;
    private String reason;
    private String evidence;
    private String status;
    private Long reviewAdminId;
    private String reviewAdminName;
    private String reviewReply;
    private LocalDateTime reviewTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static ViolationAppealVO from(ViolationAppeal appeal, User user, User admin) {
        ViolationAppealVO vo = new ViolationAppealVO();
        vo.setId(appeal.getId());
        vo.setViolationId(appeal.getViolationId());
        vo.setUserId(appeal.getUserId());
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setRealName(user.getRealName());
        }
        vo.setReason(appeal.getReason());
        vo.setEvidence(appeal.getEvidence());
        vo.setStatus(appeal.getStatus());
        vo.setReviewAdminId(appeal.getReviewAdminId());
        if (admin != null) {
            vo.setReviewAdminName(admin.getRealName() == null ? admin.getUsername() : admin.getRealName());
        }
        vo.setReviewReply(appeal.getReviewReply());
        vo.setReviewTime(appeal.getReviewTime());
        vo.setCreateTime(appeal.getCreateTime());
        vo.setUpdateTime(appeal.getUpdateTime());
        return vo;
    }
}
