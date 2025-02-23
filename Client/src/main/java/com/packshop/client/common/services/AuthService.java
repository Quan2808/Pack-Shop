package com.packshop.client.common.services;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.packshop.client.dto.identity.AuthRegisterRequest;
import com.packshop.client.dto.identity.AuthRequest;
import com.packshop.client.dto.identity.AuthResponse;

import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("null")
@Slf4j
@Service
public class AuthService {

    private final RestTemplate restTemplate;
    private static final String IDENTITY_API_URL = "http://localhost:8080/api/auth";

    public AuthService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public AuthResponse login(String username, String password) {
        String url = IDENTITY_API_URL + "/login";
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername(username);
        authRequest.setPassword(password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AuthRequest> request = new HttpEntity<>(authRequest, headers);

        try {
            ResponseEntity<AuthResponse> response = restTemplate.postForEntity(url, request, AuthResponse.class);
            AuthResponse authResponse = response.getBody();
            if (authResponse == null) {
                log.error("Login failed: Empty response from API");
                return new AuthResponse(null, null, null, null, null, "Login failed: No response from server");
            }
            log.debug("Login response: {}", authResponse);
            return authResponse;
        } catch (HttpClientErrorException e) {
            log.error("Login failed with status {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            AuthResponse errorResponse = e.getResponseBodyAs(AuthResponse.class);
            if (errorResponse != null && errorResponse.getMessage() != null) {
                return errorResponse;
            }
            String errorMessage = e.getResponseBodyAsString().isEmpty()
                    ? "Invalid username or password"
                    : "Login failed: " + e.getResponseBodyAsString();
            return new AuthResponse(null, null, null, null, null, errorMessage);
        } catch (Exception e) {
            log.error("Login failed due to unexpected error: {}", e.getMessage());
            return new AuthResponse(null, null, null, null, null, "Login failed: " + e.getMessage());
        }
    }

    public AuthResponse register(String username, String email, String password, String fullName) {
        String url = IDENTITY_API_URL + "/register";
        AuthRegisterRequest authRequest = new AuthRegisterRequest();
        authRequest.setUsername(username);
        authRequest.setEmail(email);
        authRequest.setPassword(password);
        authRequest.setFullName(fullName);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AuthRegisterRequest> request = new HttpEntity<>(authRequest, headers);

        try {
            ResponseEntity<AuthResponse> response = restTemplate.postForEntity(url, request, AuthResponse.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.error("Registration failed: {}", e.getResponseBodyAsString());
            throw new RuntimeException(e.getResponseBodyAs(AuthResponse.class).getMessage());
        }
    }

    public AuthResponse getCurrentUser(String token) {
        String url = IDENTITY_API_URL + "/me";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<AuthResponse> response = restTemplate.exchange(url, HttpMethod.GET, request,
                    AuthResponse.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.error("Get current user failed: {}", e.getResponseBodyAsString());
            throw new RuntimeException(e.getResponseBodyAs(AuthResponse.class).getMessage());
        }
    }

    public AuthResponse refreshToken(String refreshToken) {
        String url = IDENTITY_API_URL + "/refresh";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + refreshToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<AuthResponse> response = restTemplate.postForEntity(url, request, AuthResponse.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.error("Refresh token failed: {}", e.getResponseBodyAsString());
            throw new RuntimeException(e.getResponseBodyAs(AuthResponse.class).getMessage());
        }
    }
}