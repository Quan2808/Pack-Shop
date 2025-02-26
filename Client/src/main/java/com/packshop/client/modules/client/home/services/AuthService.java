package com.packshop.client.modules.client.home.services;

import java.io.IOException;
import java.util.Objects;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.packshop.client.common.exceptions.ApiException;
import com.packshop.client.common.services.ApiBaseService;
import com.packshop.client.common.utilities.FileStorageService;
import com.packshop.client.dto.identity.AuthRequest;
import com.packshop.client.dto.identity.AuthResponse;
import com.packshop.client.dto.identity.SignupRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AuthService extends ApiBaseService {

    private static final String AUTH_API_URL = "auth";
    private final FileStorageService fileStorageService;

    public AuthService(RestTemplate restTemplate, ObjectMapper objectMapper,
            FileStorageService fileStorageService) {
        super(restTemplate, objectMapper);
        this.fileStorageService = fileStorageService;
    }

    public AuthResponse login(AuthRequest authRequest) {
        log.info("Attempting to login with request: {}", authRequest);
        try {
            return postToApi(AUTH_API_URL + "/login", authRequest, AuthResponse.class);
        } catch (ApiException e) {
            log.error("Login failed: {}", e.getMessage());
            if (e.getCause() instanceof HttpClientErrorException) {
                HttpClientErrorException clientError = (HttpClientErrorException) e.getCause();
                AuthResponse errorResponse = parseErrorResponse(clientError);
                if (errorResponse != null && errorResponse.getMessage() != null) {
                    return errorResponse;
                }
                String errorMessage = clientError.getResponseBodyAsString().isEmpty()
                        ? "Invalid username or password"
                        : clientError.getResponseBodyAsString();
                return AuthResponse.builder().message(errorMessage).build();
            }
            return AuthResponse.builder().message("Login failed: " + e.getMessage()).build();
        }
    }

    public AuthResponse register(SignupRequest authRequest) throws IOException {
        log.info("Registering user: {}", authRequest);

        // Tạo bản sao không chứa avatarFile
        SignupRequest apiRequest = new SignupRequest();
        BeanUtils.copyProperties(authRequest, apiRequest, "avatarFile");

        // Xử lý upload avatar nếu có
        String avatarUrl =
                handleAvatarUpload(authRequest.getAvatarFile(), authRequest.getUsername());
        apiRequest.setAvatarUrl(avatarUrl);

        try {
            AuthResponse response =
                    postToApi(AUTH_API_URL + "/register", apiRequest, AuthResponse.class);
            log.info("Registration successful: {}", response);
            return response;
        } catch (ApiException e) {
            log.error("Registration failed: {}", e.getMessage());
            if (e.getCause() instanceof HttpClientErrorException) {
                HttpClientErrorException clientError = (HttpClientErrorException) e.getCause();
                AuthResponse errorResponse = parseErrorResponse(clientError);
                if (errorResponse != null && errorResponse.getMessage() != null) {
                    return errorResponse;
                }
                String errorMessage = clientError.getResponseBodyAsString().isEmpty()
                        ? "Registration failed: Invalid input"
                        : clientError.getResponseBodyAsString();
                return AuthResponse.builder().message(errorMessage).build();
            }
            return AuthResponse.builder().message("Registration failed: " + e.getMessage()).build();
        }
    }

    public AuthResponse getCurrentUser(String token) {
        log.info("Fetching current user with token");
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<AuthResponse> response =
                    restTemplate.exchange(BASE_API_URL + AUTH_API_URL + "/me", HttpMethod.GET,
                            entity, AuthResponse.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new ApiException("Failed to fetch current user",
                        response.getStatusCode().value());
            }
            return Objects.requireNonNull(response.getBody(), "Response body is null");
        } catch (ApiException e) {
            log.error("Get current user failed: {}", e.getMessage());
            if (e.getCause() instanceof HttpClientErrorException) {
                HttpClientErrorException clientError = (HttpClientErrorException) e.getCause();
                String errorMessage = clientError.getResponseBodyAsString().isEmpty()
                        ? "Failed to fetch user: Unauthorized"
                        : clientError.getResponseBodyAsString();
                throw new RuntimeException(errorMessage);
            }
            throw new RuntimeException("Failed to fetch user: " + e.getMessage());
        }
    }

    public AuthResponse updateProfile(String token, SignupRequest request) throws IOException {
        log.info("Updating profile with request: {}", request);

        // Tạo bản sao không chứa avatarFile và password
        SignupRequest apiRequest = new SignupRequest();
        BeanUtils.copyProperties(request, apiRequest, "avatarFile", "password");

        // Xử lý upload avatar nếu có
        String avatarUrl = handleAvatarUpload(request.getAvatarFile(), request.getUsername());
        apiRequest.setAvatarUrl(avatarUrl);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);
            HttpEntity<SignupRequest> entity = new HttpEntity<>(apiRequest, headers);

            ResponseEntity<AuthResponse> response =
                    restTemplate.exchange(BASE_API_URL + AUTH_API_URL + "/profile/update",
                            HttpMethod.POST, entity, AuthResponse.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new ApiException("Failed to update profile",
                        response.getStatusCode().value());
            }
            return Objects.requireNonNull(response.getBody(), "Response body is null");
        } catch (ApiException e) {
            log.error("Update profile failed: {}", e.getMessage());
            if (e.getCause() instanceof HttpClientErrorException) {
                HttpClientErrorException clientError = (HttpClientErrorException) e.getCause();
                String errorMessage = clientError.getResponseBodyAsString().isEmpty()
                        ? "Failed to update profile: Invalid input"
                        : clientError.getResponseBodyAsString();
                throw new RuntimeException(errorMessage);
            }
            throw new RuntimeException("Failed to update profile: " + e.getMessage());
        }
    }

    public AuthResponse refreshToken(String refreshToken) {
        log.info("Refreshing token");
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(refreshToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<AuthResponse> response =
                    restTemplate.exchange(BASE_API_URL + AUTH_API_URL + "/refresh", HttpMethod.POST,
                            entity, AuthResponse.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new ApiException("Failed to refresh token", response.getStatusCode().value());
            }
            return Objects.requireNonNull(response.getBody(), "Response body is null");
        } catch (ApiException e) {
            log.error("Refresh token failed: {}", e.getMessage());
            if (e.getCause() instanceof HttpClientErrorException) {
                HttpClientErrorException clientError = (HttpClientErrorException) e.getCause();
                String errorMessage = clientError.getResponseBodyAsString().isEmpty()
                        ? "Failed to refresh token: Invalid token"
                        : clientError.getResponseBodyAsString();
                throw new RuntimeException(errorMessage);
            }
            throw new RuntimeException("Failed to refresh token: " + e.getMessage());
        }
    }

    private String handleAvatarUpload(MultipartFile file, String username) throws IOException {
        if (file != null && !file.isEmpty()) {
            String path = "user/" + username;
            return fileStorageService.storeFile(file, path);
        }
        return null;
    }

    private AuthResponse parseErrorResponse(HttpClientErrorException e) {
        try {
            return objectMapper.readValue(e.getResponseBodyAsString(), AuthResponse.class);
        } catch (Exception ex) {
            log.warn("Failed to parse error response body: {}", e.getResponseBodyAsString(), ex);
            return null;
        }
    }
}
