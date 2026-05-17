package com.seveneleven.dto.auth;

import com.seveneleven.entity.UserRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {

    private String token;

    private String username;

    private String email;

    private String fullName;

    private UserRole role;

}
