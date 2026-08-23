package com.lending.platform.service;

import com.lending.platform.dto.request.LoginRequest;
import com.lending.platform.dto.response.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);
}
