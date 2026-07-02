package com.echostudy.controller;

import com.echostudy.common.Result;
import com.echostudy.dto.CreditScoreUpdateRequest;
import com.echostudy.dto.UserUpdateRequest;
import com.echostudy.entity.OperationLog;
import com.echostudy.entity.User;
import com.echostudy.enums.UserStatus;
import com.echostudy.exception.BusinessException;
import com.echostudy.mapper.OperationLogMapper;
import com.echostudy.mapper.UserMapper;
import com.echostudy.security.UserContext;
import com.echostudy.vo.UserVO;
import jakarta.validation.Valid;
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
    private final OperationLogMapper operationLogMapper;

    @GetMapping
    public Result<List<UserVO>> list() {
        return Result.ok(userMapper.selectList(null).stream().map(UserVO::from).toList());
    }

    @PutMapping("/{id}")
    public Result<UserVO> update(@PathVariable Long id, @RequestBody UserUpdateRequest request) {
        requireUser(id);
        User user = new User();
        user.setId(id);
        user.setRealName(request.getRealName());
        user.setStudentNo(request.getStudentNo());
        user.setPhone(request.getPhone());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        return Result.ok(UserVO.from(userMapper.selectById(id)));
    }

    @PutMapping("/{id}/credit-score")
    public Result<UserVO> updateCreditScore(@PathVariable Long id, @Valid @RequestBody CreditScoreUpdateRequest request) {
        User existing = requireUser(id);
        User user = new User();
        user.setId(id);
        user.setCreditScore(request.getCreditScore());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        createOperationLog("调整信用分",
                "将用户 " + displayName(existing) + " 的信用分调整为 " + request.getCreditScore()
                        + (request.getReason() == null || request.getReason().isBlank() ? "" : "，原因：" + request.getReason().trim()),
                "USER", id);
        return Result.ok(UserVO.from(userMapper.selectById(id)));
    }

    @PostMapping("/{id}/ban")
    public Result<UserVO> ban(@PathVariable Long id) {
        return Result.ok(updateStatus(id, UserStatus.BANNED.name(), LocalDateTime.now().plusDays(3)));
    }

    @PostMapping("/{id}/unban")
    public Result<UserVO> unban(@PathVariable Long id) {
        requireUser(id);
        User user = new User();
        user.setId(id);
        user.setStatus(UserStatus.NORMAL.name());
        user.setBanEndTime(null);
        user.setViolationCount(0);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        createOperationLog("解除封禁", "解除用户 #" + id + " 的封禁状态", "USER", id);
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
        User existing = requireUser(id);
        User user = new User();
        user.setId(id);
        user.setStatus(status);
        user.setBanEndTime(banEndTime);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        createOperationLog("调整用户状态", "将用户 " + displayName(existing) + " 状态调整为 " + status, "USER", id);
        return UserVO.from(userMapper.selectById(id));
    }

    private User requireUser(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    private String displayName(User user) {
        return user.getRealName() == null || user.getRealName().isBlank() ? user.getUsername() : user.getRealName();
    }

    private void createOperationLog(String operationType, String operationContent, String targetType, Long targetId) {
        OperationLog log = new OperationLog();
        log.setAdminId(UserContext.getUserId());
        log.setOperationType(operationType);
        log.setOperationContent(operationContent);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setCreateTime(LocalDateTime.now());
        operationLogMapper.insert(log);
    }
}
