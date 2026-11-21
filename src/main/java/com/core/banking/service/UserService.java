package com.core.banking.service;

import com.core.banking.dto.request.RegisterRequest;
import com.core.banking.dto.response.UserResponse;

public interface UserService {

    UserResponse register(RegisterRequest request);
}