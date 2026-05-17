package com.seveneleven.controller;

import com.seveneleven.dto.ApiResponse;
import com.seveneleven.dto.auth.AuthResponse;
import com.seveneleven.dto.auth.LoginRequest;
import com.seveneleven.dto.auth.RegisterRequest;
import com.seveneleven.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("#AuthController.register - username: {}, email: {}", request.getUsername(), request.getEmail());

        ApiResponse<AuthResponse> response = ApiResponse.ok("Registered successfully", authService.register(request));
        log.info("#AuthController.register - success for username: {}", request.getUsername());

        // TODO: identify email through aws to use SES Sandbox

        return response;
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("#AuthController.login - username: {}", request.getUsername());

        ApiResponse<AuthResponse> response = ApiResponse.ok("Login successful", authService.login(request));
        log.info("#AuthController.login - success for username: {}", request.getUsername());

        return response;
    }
}
