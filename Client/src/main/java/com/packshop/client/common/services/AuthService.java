package com.packshop.client.common.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.packshop.client.dto.identity.AuthRequest;
import com.packshop.client.dto.identity.AuthResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AuthService {

    private final RestTemplate restTemplate;

    private String IDENTITY_API_URL = "http://localhost:8080/api";

    public AuthService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public AuthResponse login(String username, String password) {
        String url = IDENTITY_API_URL + "/auth/login";
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername(username);
        authRequest.setPassword(password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AuthRequest> request = new HttpEntity<>(authRequest, headers);

        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(url, request, AuthResponse.class);
        return response.getBody();
    }

    public AuthResponse register(String username, String email, String password) {
    String url = IDENTITY_API_URL + "/auth/register";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    Map<String, String> userMap = new HashMap<>();
    userMap.put("username", username);
    userMap.put("email", email);
    userMap.put("password", password);
    HttpEntity<Map<String, String>> request = new HttpEntity<>(userMap, headers);

    ResponseEntity<AuthResponse> response = restTemplate.postForEntity(url, request, AuthResponse.class);
    return response.getBody();
}

    public AuthResponse getCurrentUser(String token) {
        String url = IDENTITY_API_URL + "/auth/me";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<AuthResponse> response = restTemplate.exchange(url, HttpMethod.GET, request, AuthResponse.class);
        return response.getBody();
    }

    public AuthResponse refreshToken(String refreshToken) {
        String url = IDENTITY_API_URL + "/auth/refresh";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + refreshToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(url, request, AuthResponse.class);
        return response.getBody();
    }
}