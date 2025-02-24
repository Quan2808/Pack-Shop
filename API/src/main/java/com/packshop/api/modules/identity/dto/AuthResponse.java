package com.packshop.api.modules.identity.dto;

import java.util.Set;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private Long userId;
    private String username;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String avatarUrl;
    private Set<String> roles;
    private String token;
    private String refreshToken;
    private String message;
}
