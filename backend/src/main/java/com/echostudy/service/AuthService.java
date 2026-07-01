package com.echostudy.service;

import com.echostudy.dto.LoginRequest;
import com.echostudy.dto.RegisterRequest;
import com.echostudy.dto.AdminRegisterRequest;
import com.echostudy.dto.ForgotPasswordCodeRequest;
import com.echostudy.dto.ForgotPasswordResetRequest;
import com.echostudy.vo.LoginResponse;
import com.echostudy.vo.UserVO;

public interface AuthService {

    UserVO register(RegisterRequest request);

    UserVO registerAdmin(AdminRegisterRequest request);

    LoginResponse login(LoginRequest request);

    UserVO me();

    String sendForgotPasswordCode(ForgotPasswordCodeRequest request);

    void resetPassword(ForgotPasswordResetRequest request);
}
