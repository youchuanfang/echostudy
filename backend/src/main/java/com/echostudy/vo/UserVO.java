package com.echostudy.vo;

import com.echostudy.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVO {

    private Long id;
    private String username;
    private String realName;
    private String studentNo;
    private String phone;
    private String role;
    private String status;
    private Integer violationCount;
    private Integer creditScore;
    private LocalDateTime banEndTime;
    private Boolean canRegisterAdmin;

    public static UserVO from(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setStudentNo(user.getStudentNo());
        vo.setPhone(user.getPhone());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        vo.setViolationCount(user.getViolationCount());
        vo.setCreditScore(user.getCreditScore());
        vo.setBanEndTime(user.getBanEndTime());
        vo.setCanRegisterAdmin(user.getCanRegisterAdmin());
        return vo;
    }
}
