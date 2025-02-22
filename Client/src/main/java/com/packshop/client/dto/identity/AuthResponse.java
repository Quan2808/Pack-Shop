package com.packshop.client.dto.identity;

import java.util.Set;

import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private String refreshToken;
    private String username;
    private Set<String> roles;
    private String message;
}
