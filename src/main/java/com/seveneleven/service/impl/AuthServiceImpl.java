package com.seveneleven.service.impl;

import com.seveneleven.dto.auth.AuthResponse;
import com.seveneleven.dto.auth.LoginRequest;
import com.seveneleven.dto.auth.RegisterRequest;
import com.seveneleven.entity.User;
import com.seveneleven.entity.UserRole;
import com.seveneleven.exception.BadRequestException;
import com.seveneleven.repository.UserRepository;
import com.seveneleven.security.JwtUtil;
import com.seveneleven.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("#AuthServiceImpl.register - START - username: {}, email: {}", request.getUsername(), request.getEmail());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already in use");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .role(UserRole.USER)
                .build();
        userRepository.save(user);
        log.info("#AuthServiceImpl.register - registered user: {}", request.getUsername());

        return AuthResponse.builder()
                .token(jwtUtil.generateToken(user))
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("#AuthServiceImpl.login - START - username: {}", request.getUsername());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        User user = userRepository.findByUsername(request.getUsername()).orElseThrow();
        log.info("#AuthServiceImpl.login - login success for username: {}, role: {}", user.getUsername(), user.getRole());

        return AuthResponse.builder()
                .token(jwtUtil.generateToken(user))
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }

}
