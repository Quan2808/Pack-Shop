package com.packshop.api.modules.identity.dto;

import java.util.Set;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String token;
    private String refreshToken;
    private String username;
    private String fullName;
    private Set<String> roles;
    private String message;
}
