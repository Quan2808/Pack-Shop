package com.packshop.client.modules.client.home.services;

import java.io.IOException;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import com.packshop.client.common.utilities.FileStorageService;
import com.packshop.client.dto.identity.AuthRegisterRequest;
import com.packshop.client.dto.identity.AuthRequest;
import com.packshop.client.dto.identity.AuthResponse;
import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("null")
@Slf4j
@Service
public class AuthService {

    private static final String IDENTITY_API_URL = "http://localhost:8080/api/auth";
    private final RestTemplate restTemplate;
    private final FileStorageService fileStorageService;

    public AuthService(RestTemplate restTemplate, FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
        this.restTemplate = restTemplate;
    }

    public AuthResponse login(AuthRequest authRequest) {
        String url = IDENTITY_API_URL + "/login";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AuthRequest> request = new HttpEntity<>(authRequest, headers);

        try {
            ResponseEntity<AuthResponse> response =
                    restTemplate.postForEntity(url, request, AuthResponse.class);
            AuthResponse authResponse = response.getBody();
            if (authResponse == null) {
                log.error("Login failed: Empty response from API");
                return AuthResponse.builder().message("Login failed: No response from server")
                        .build();
            }
            log.info("Login response: {}", authResponse);
            return authResponse;
        } catch (HttpClientErrorException e) {
            log.error("Login failed with status {}: {}", e.getStatusCode(),
                    e.getResponseBodyAsString());
            AuthResponse errorResponse = e.getResponseBodyAs(AuthResponse.class);
            if (errorResponse != null && errorResponse.getMessage() != null) {
                return errorResponse;
            }
            String errorMessage =
                    e.getResponseBodyAsString().isEmpty() ? "Invalid username or password"
                            : "Login failed: " + e.getResponseBodyAsString();
            return AuthResponse.builder().message(errorMessage).build();
        } catch (Exception e) {
            log.error("Login failed due to unexpected error: {}", e.getMessage());
            return AuthResponse.builder().message("Login failed: " + e.getMessage()).build();
        }
    }

    public AuthResponse register(AuthRegisterRequest authRequest) throws IOException {
        String url = IDENTITY_API_URL + "/register";

        // Tạo một bản sao của authRequest để gửi API, không chứa avatarFile
        AuthRegisterRequest apiRequest = new AuthRegisterRequest();
        BeanUtils.copyProperties(authRequest, apiRequest, "avatarFile"); // Sao chép các field trừ
                                                                         // avatarFile

        // Xử lý upload avatar nếu có
        String avatarUrl =
                handleAvatarUpload(authRequest.getAvatarFile(), authRequest.getUsername());
        apiRequest.setAvatarUrl(avatarUrl); // Gán URL của avatar sau khi upload

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AuthRegisterRequest> request = new HttpEntity<>(apiRequest, headers);

        try {
            ResponseEntity<AuthResponse> response =
                    restTemplate.postForEntity(url, request, AuthResponse.class);
            AuthResponse authResponse = response.getBody();
            if (authResponse == null) {
                log.error("Registration failed: Empty response from API");
                return AuthResponse.builder()
                        .message("Registration failed: No response from server").build();
            }
            log.info("Registration response: {}", authResponse);
            return authResponse;
        } catch (HttpClientErrorException e) {
            log.error("Registration failed with status {}: {}", e.getStatusCode(),
                    e.getResponseBodyAsString());
            AuthResponse errorResponse = e.getResponseBodyAs(AuthResponse.class);
            if (errorResponse != null && errorResponse.getMessage() != null) {
                return errorResponse;
            }
            String errorMessage =
                    e.getResponseBodyAsString().isEmpty() ? "Registration failed: Invalid input"
                            : "Registration failed: " + e.getResponseBodyAsString();
            return AuthResponse.builder().message(errorMessage).build();
        } catch (Exception e) {
            log.error("Registration failed due to unexpected error: {}", e.getMessage());
            return AuthResponse.builder().message("Registration failed: " + e.getMessage()).build();
        }
    }

    public AuthResponse getCurrentUser(String token) {
        String url = IDENTITY_API_URL + "/me";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<AuthResponse> response =
                    restTemplate.exchange(url, HttpMethod.GET, request, AuthResponse.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.error("Get current user failed: {}", e.getResponseBodyAsString());
            throw new RuntimeException(e.getResponseBodyAs(AuthResponse.class).getMessage());
        }
    }

    public AuthResponse updateProfile(String token, AuthRegisterRequest request)
            throws IOException {
        String url = IDENTITY_API_URL + "/profile/update";
        AuthRegisterRequest apiRequest = new AuthRegisterRequest();
        BeanUtils.copyProperties(request, apiRequest, "avatarFile", "password");

        String avatarUrl = handleAvatarUpload(request.getAvatarFile(), request.getUsername());
        apiRequest.setAvatarUrl(avatarUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        HttpEntity<AuthRegisterRequest> httpRequest = new HttpEntity<>(apiRequest, headers);

        ResponseEntity<AuthResponse> response =
                restTemplate.postForEntity(url, httpRequest, AuthResponse.class);
        AuthResponse authResponse = response.getBody();
        if (authResponse == null) {
            throw new IOException("Update failed: No response from server");
        }
        return authResponse;
    }

    public AuthResponse refreshToken(String refreshToken) {
        String url = IDENTITY_API_URL + "/refresh";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + refreshToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<AuthResponse> response =
                    restTemplate.postForEntity(url, request, AuthResponse.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.error("Refresh token failed: {}", e.getResponseBodyAsString());
            throw new RuntimeException(e.getResponseBodyAs(AuthResponse.class).getMessage());
        }
    }

    private String handleAvatarUpload(MultipartFile file, String username) throws IOException {
        if (file != null && !file.isEmpty()) {
            String path = "user/" + username;
            return fileStorageService.storeFile(file, path);
        }
        return null;
    }
}
