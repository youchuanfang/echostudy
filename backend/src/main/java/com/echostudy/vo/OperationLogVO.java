package com.echostudy.vo;

import com.echostudy.entity.OperationLog;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OperationLogVO {
    private Long id;
    private Long adminId;
    private String adminName;
    private String operationType;
    private String operationContent;
    private String targetType;
    private Long targetId;
    private LocalDateTime createTime;

    public static OperationLogVO from(OperationLog log) {
        OperationLogVO vo = new OperationLogVO();
        vo.setId(log.getId());
        vo.setAdminId(log.getAdminId());
        vo.setOperationType(log.getOperationType());
        vo.setOperationContent(log.getOperationContent());
        vo.setTargetType(log.getTargetType());
        vo.setTargetId(log.getTargetId());
        vo.setCreateTime(log.getCreateTime());
        return vo;
    }
}
