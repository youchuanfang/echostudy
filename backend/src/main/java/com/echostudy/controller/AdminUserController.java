package com.echostudy.controller;

import com.echostudy.common.Result;
import com.echostudy.dto.UserUpdateRequest;
import com.echostudy.entity.User;
import com.echostudy.enums.UserStatus;
import com.echostudy.mapper.UserMapper;
import com.echostudy.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserMapper userMapper;

    @GetMapping
    public Result<List<UserVO>> list() {
        return Result.ok(userMapper.selectList(null).stream().map(UserVO::from).toList());
    }

    @PutMapping("/{id}")
    public Result<UserVO> update(@PathVariable Long id, @RequestBody UserUpdateRequest request) {
        User user = new User();
        user.setId(id);
        user.setRealName(request.getRealName());
        user.setStudentNo(request.getStudentNo());
        user.setPhone(request.getPhone());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        return Result.ok(UserVO.from(userMapper.selectById(id)));
    }

    @PostMapping("/{id}/ban")
    public Result<UserVO> ban(@PathVariable Long id) {
        return Result.ok(updateStatus(id, UserStatus.BANNED.name(), LocalDateTime.now().plusDays(3)));
    }

    @PostMapping("/{id}/unban")
    public Result<UserVO> unban(@PathVariable Long id) {
        User user = new User();
        user.setId(id);
        user.setStatus(UserStatus.NORMAL.name());
        user.setBanEndTime(null);
        user.setViolationCount(0);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        return Result.ok(UserVO.from(userMapper.selectById(id)));
    }

    @PostMapping("/{id}/disable")
    public Result<UserVO> disable(@PathVariable Long id) {
        return Result.ok(updateStatus(id, UserStatus.DISABLED.name(), null));
    }

    @PostMapping("/{id}/enable")
    public Result<UserVO> enable(@PathVariable Long id) {
        return Result.ok(updateStatus(id, UserStatus.NORMAL.name(), null));
    }

    private UserVO updateStatus(Long id, String status, LocalDateTime banEndTime) {
        User user = new User();
        user.setId(id);
        user.setStatus(status);
        user.setBanEndTime(banEndTime);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        return UserVO.from(userMapper.selectById(id));
    }
}
