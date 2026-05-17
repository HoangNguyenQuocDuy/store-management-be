package com.seveneleven.service;

import com.seveneleven.dto.auth.AuthResponse;
import com.seveneleven.dto.auth.LoginRequest;
import com.seveneleven.dto.auth.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

}
