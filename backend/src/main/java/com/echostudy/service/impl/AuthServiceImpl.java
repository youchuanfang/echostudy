package com.echostudy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.echostudy.dto.LoginRequest;
import com.echostudy.dto.RegisterRequest;
import com.echostudy.entity.User;
import com.echostudy.enums.UserRole;
import com.echostudy.enums.UserStatus;
import com.echostudy.exception.BusinessException;
import com.echostudy.mapper.UserMapper;
import com.echostudy.security.JwtUtils;
import com.echostudy.security.UserContext;
import com.echostudy.service.AuthService;
import com.echostudy.vo.LoginResponse;
import com.echostudy.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final JwtUtils jwtUtils;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public UserVO register(RegisterRequest request) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setStudentNo(request.getStudentNo());
        user.setPhone(request.getPhone());
        user.setRole(UserRole.STUDENT.name());
        user.setStatus(UserStatus.NORMAL.name());
        user.setViolationCount(0);
        user.setCreateTime(LocalDateTime.now());
        userMapper.insert(user);
        return UserVO.from(user);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (UserStatus.DISABLED.name().equals(user.getStatus())) {
            throw new BusinessException(403, "账号已禁用");
        }
        return new LoginResponse(jwtUtils.generateToken(user.getId(), user.getRole()), UserVO.from(user));
    }

    @Override
    public UserVO me() {
        User user = userMapper.selectById(UserContext.getUserId());
        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }
        return UserVO.from(user);
    }
}
