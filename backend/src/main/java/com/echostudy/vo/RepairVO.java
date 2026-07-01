package com.echostudy.vo;

import com.echostudy.entity.RepairRecord;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RepairVO {

    private Long id;
    private Long userId;
    private String username;
    private String realName;
    private Long roomId;
    private String roomName;
    private String spaceType;
    private Long seatId;
    private String seatNo;
    private String repairLevel;
    private String repairType;
    private String description;
    private String status;
    private Long adminId;
    private String adminName;
    private String adminReply;
    private LocalDateTime createTime;
    private LocalDateTime acceptTime;
    private LocalDateTime processTime;
    private LocalDateTime finishTime;
    private LocalDateTime cancelTime;
    private LocalDateTime updateTime;

    public static RepairVO from(RepairRecord repair) {
        RepairVO vo = new RepairVO();
        vo.setId(repair.getId());
        vo.setUserId(repair.getUserId());
        vo.setRoomId(repair.getRoomId());
        vo.setSeatId(repair.getSeatId());
        vo.setRepairLevel(repair.getRepairLevel());
        vo.setRepairType(repair.getRepairType());
        vo.setDescription(repair.getDescription());
        vo.setStatus(repair.getStatus());
        vo.setAdminId(repair.getAdminId());
        vo.setAdminReply(repair.getAdminReply());
        vo.setCreateTime(repair.getCreateTime());
        vo.setAcceptTime(repair.getAcceptTime());
        vo.setProcessTime(repair.getProcessTime());
        vo.setFinishTime(repair.getFinishTime());
        vo.setCancelTime(repair.getCancelTime());
        vo.setUpdateTime(repair.getUpdateTime());
        return vo;
    }
}
