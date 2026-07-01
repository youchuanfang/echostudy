package com.echostudy.vo;

import com.echostudy.entity.Notification;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationVO {

    private Long id;
    private Long userId;
    private String username;
    private String realName;
    private String title;
    private String content;
    private String type;
    private Boolean readStatus;
    private String relatedType;
    private Long relatedId;
    private LocalDateTime createTime;
    private LocalDateTime readTime;

    public static NotificationVO from(Notification notification) {
        NotificationVO vo = new NotificationVO();
        vo.setId(notification.getId());
        vo.setUserId(notification.getUserId());
        vo.setTitle(notification.getTitle());
        vo.setContent(notification.getContent());
        vo.setType(notification.getType());
        vo.setReadStatus(notification.getReadStatus());
        vo.setRelatedType(notification.getRelatedType());
        vo.setRelatedId(notification.getRelatedId());
        vo.setCreateTime(notification.getCreateTime());
        vo.setReadTime(notification.getReadTime());
        return vo;
    }
}
