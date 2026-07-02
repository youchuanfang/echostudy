package com.echostudy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.echostudy.dto.AdminRegisterRequest;
import com.echostudy.dto.ForgotPasswordCodeRequest;
import com.echostudy.dto.ForgotPasswordResetRequest;
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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Pattern CHINA_MOBILE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[^A-Za-z0-9]");
    private static final String RESERVED_RESET_CODE = "123456";
    private final Map<String, String> forgotPasswordCodes = new ConcurrentHashMap<>();

    private final UserMapper userMapper;
    private final JwtUtils jwtUtils;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public UserVO register(RegisterRequest request) {
        validatePassword(request.getPassword());
        validatePhone(request.getPhone());
        validateStudentNo(request.getStudentNo());
        ensureUsernameAvailable(request.getUsername(), UserRole.STUDENT.name());
        ensureStudentNoAvailable(request.getStudentNo());
        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(trimToNull(request.getRealName()));
        user.setStudentNo(request.getStudentNo().trim());
        user.setPhone(request.getPhone().trim());
        user.setRole(UserRole.STUDENT.name());
        user.setStatus(UserStatus.NORMAL.name());
        user.setViolationCount(0);
        user.setCreditScore(100);
        user.setCanRegisterAdmin(false);
        user.setCreateTime(LocalDateTime.now());
        userMapper.insert(user);
        return UserVO.from(user);
    }

    @Override
    public UserVO registerAdmin(AdminRegisterRequest request) {
        requireAdminRegisterPermission();
        validatePassword(request.getPassword());
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            validatePhone(request.getPhone());
        }
        ensureUsernameAvailable(request.getUsername(), UserRole.ADMIN.name());

        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(trimToNull(request.getRealName()));
        user.setPhone(trimToNull(request.getPhone()));
        user.setRole(UserRole.ADMIN.name());
        user.setStatus(UserStatus.NORMAL.name());
        user.setViolationCount(0);
        user.setCreditScore(100);
        user.setCanRegisterAdmin(Boolean.TRUE.equals(request.getCanRegisterAdmin()));
        user.setCreateTime(LocalDateTime.now());
        userMapper.insert(user);
        return UserVO.from(user);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = findLoginUser(request);
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "账号或密码错误");
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

    @Override
    public String sendForgotPasswordCode(ForgotPasswordCodeRequest request) {
        validatePhone(request.getPhone());
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getRole, normalizeRole(request.getRole()))
                .eq(User::getPhone, request.getPhone().trim()));
        if (user == null) {
            throw new BusinessException("未找到绑定该手机号的账号");
        }
        forgotPasswordCodes.put(resetCodeKey(request.getRole(), request.getPhone()), RESERVED_RESET_CODE);
        return "验证码功能已预留，当前测试验证码为 123456";
    }

    @Override
    public void resetPassword(ForgotPasswordResetRequest request) {
        validatePhone(request.getPhone());
        validatePassword(request.getNewPassword());
        String key = resetCodeKey(request.getRole(), request.getPhone());
        if (!request.getCode().trim().equals(forgotPasswordCodes.get(key))) {
            throw new BusinessException("验证码错误");
        }
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getRole, normalizeRole(request.getRole()))
                .eq(User::getPhone, request.getPhone().trim()));
        if (user == null) {
            throw new BusinessException("未找到绑定该手机号的账号");
        }
        User update = new User();
        update.setId(user.getId());
        update.setPassword(passwordEncoder.encode(request.getNewPassword()));
        update.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(update);
        forgotPasswordCodes.remove(key);
    }

    private User findLoginUser(LoginRequest request) {
        String role = normalizeRole(request.getRole());
        String loginType = request.getLoginType() == null ? "USERNAME" : request.getLoginType().trim().toUpperCase();
        String account = request.getAccount() == null || request.getAccount().isBlank()
                ? request.getUsername()
                : request.getAccount();
        if (account == null || account.isBlank()) {
            throw new BusinessException("请输入登录账号");
        }
        account = account.trim();
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<User>().eq(User::getRole, role);
        if ("PHONE".equals(loginType)) {
            query.eq(User::getPhone, account);
        } else if ("STUDENT_NO".equals(loginType)) {
            query.eq(User::getStudentNo, account);
        } else {
            query.eq(User::getUsername, account);
        }
        return userMapper.selectOne(query);
    }

    private void ensureUsernameAvailable(String username, String role) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getRole, role)
                .eq(User::getUsername, username.trim()));
        if (count > 0) {
            throw new BusinessException("该用户名已存在无法被注册");
        }
    }

    private void ensureStudentNoAvailable(String studentNo) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getRole, UserRole.STUDENT.name())
                .eq(User::getStudentNo, studentNo.trim()));
        if (count > 0) {
            throw new BusinessException("该学号学生已经注册");
        }
    }

    private void validateStudentNo(String studentNo) {
        if (studentNo == null || !studentNo.matches("\\d{10}")) {
            throw new BusinessException("学号必须是10位数字");
        }
    }

    private void validatePhone(String phone) {
        if (phone == null || !CHINA_MOBILE_PATTERN.matcher(phone.trim()).matches()) {
            throw new BusinessException("请输入正确手机号！");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new BusinessException("密码至少8位，且需包含数字、字母、特殊符号中的至少两类");
        }
        int types = 0;
        if (password.matches(".*\\d.*")) {
            types++;
        }
        if (password.matches(".*[A-Za-z].*")) {
            types++;
        }
        if (SPECIAL_CHAR_PATTERN.matcher(password).find()) {
            types++;
        }
        if (types < 2) {
            throw new BusinessException("密码至少8位，且需包含数字、字母、特殊符号中的至少两类");
        }
    }

    private void requireAdminRegisterPermission() {
        Long userId = UserContext.getUserId();
        if (userId == null || !"ADMIN".equals(UserContext.getRole())) {
            throw new BusinessException(403, "不好意思，您无此权限");
        }
        User admin = userMapper.selectById(userId);
        if (admin == null || !Boolean.TRUE.equals(admin.getCanRegisterAdmin())) {
            throw new BusinessException(403, "不好意思，您无此权限");
        }
    }

    private String normalizeRole(String role) {
        String value = role == null ? "" : role.trim().toUpperCase();
        if (!UserRole.STUDENT.name().equals(value) && !UserRole.ADMIN.name().equals(value)) {
            throw new BusinessException("登录身份不正确");
        }
        return value;
    }

    private String resetCodeKey(String role, String phone) {
        return normalizeRole(role) + ":" + phone.trim();
    }

    private String trimToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}
