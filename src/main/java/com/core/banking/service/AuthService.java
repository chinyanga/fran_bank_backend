package com.core.banking.service;

import com.core.banking.dto.request.LoginRequest;
import com.core.banking.dto.request.RegisterRequest;
import com.core.banking.dto.response.AuthTokenResult;
import com.core.banking.dto.response.UserResponse;

public interface AuthService {

    UserResponse register(RegisterRequest request);

    AuthTokenResult login(LoginRequest request);
}