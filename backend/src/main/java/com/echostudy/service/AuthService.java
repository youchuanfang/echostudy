package com.echostudy.service;

import com.echostudy.dto.LoginRequest;
import com.echostudy.dto.RegisterRequest;
import com.echostudy.vo.LoginResponse;
import com.echostudy.vo.UserVO;

public interface AuthService {

    UserVO register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    UserVO me();
}
