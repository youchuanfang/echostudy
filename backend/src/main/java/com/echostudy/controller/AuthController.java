package com.echostudy.controller;

import com.echostudy.common.Result;
import com.echostudy.dto.AdminRegisterRequest;
import com.echostudy.dto.ForgotPasswordCodeRequest;
import com.echostudy.dto.ForgotPasswordResetRequest;
import com.echostudy.dto.LoginRequest;
import com.echostudy.dto.RegisterRequest;
import com.echostudy.service.AuthService;
import com.echostudy.vo.LoginResponse;
import com.echostudy.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody RegisterRequest request) {
        return Result.ok(authService.register(request));
    }

    @PostMapping("/admin/register")
    public Result<UserVO> registerAdmin(@Valid @RequestBody AdminRegisterRequest request) {
        return Result.ok(authService.registerAdmin(request));
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(authService.login(request));
    }

    @PostMapping("/forgot-password/code")
    public Result<String> forgotPasswordCode(@Valid @RequestBody ForgotPasswordCodeRequest request) {
        return Result.ok(authService.sendForgotPasswordCode(request));
    }

    @PostMapping("/forgot-password/reset")
    public Result<Void> resetPassword(@Valid @RequestBody ForgotPasswordResetRequest request) {
        authService.resetPassword(request);
        return Result.ok();
    }

    @GetMapping("/me")
    public Result<UserVO> me() {
        return Result.ok(authService.me());
    }
}
