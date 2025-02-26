package com.packshop.client.modules.client.home.services;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
import com.packshop.client.dto.identity.UpdateAccountRequest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AuthService extends ApiBaseService {

    private static final String AUTH_API_URL = "auth";
    private final FileStorageService fileStorageService;

    public AuthService(RestTemplate restTemplate, ObjectMapper objectMapper, FileStorageService fileStorageService) {
        super(restTemplate, objectMapper);
        this.fileStorageService = fileStorageService;
    }

    public AuthResponse login(AuthRequest authRequest) {
        log.info("Attempting login with request: {}", authRequest);
        return executePost(AUTH_API_URL + "/login", authRequest, AuthResponse.class, "Invalid username or password");
    }

    public AuthResponse register(SignupRequest authRequest) throws IOException {
        log.info("Registering user: {}", authRequest);

        SignupRequest apiRequest = new SignupRequest();
        BeanUtils.copyProperties(authRequest, apiRequest, "avatarFile");
        apiRequest.setAvatarUrl(handleAvatarUpload(authRequest.getAvatarFile(), authRequest.getUsername()));

        return executePost(AUTH_API_URL + "/register", apiRequest, AuthResponse.class, "Invalid input");
    }

    public AuthResponse getCurrentUser(String token) {
        log.info("Fetching current user with token");
        HttpHeaders headers = createBearerHeaders(token);
        return executeRequest(HttpMethod.GET, AUTH_API_URL + "/me", null, headers, AuthResponse.class, "Unauthorized");
    }

    public AuthResponse updateProfile(UpdateAccountRequest request, String username) throws IOException {
        log.info("Updating profile for user: {}", username);

        AuthResponse currentUser = getCurrentUser(request.getToken());
        String oldAvatarUrl = currentUser.getAvatarUrl();
        String newAvatarUrl = manageAvatar(request.getAvatarFile(), username, oldAvatarUrl);

        Map<String, Object> apiRequest = new HashMap<>();
        apiRequest.put("email", request.getEmail());
        apiRequest.put("fullName", request.getFullName());
        apiRequest.put("phoneNumber", request.getPhoneNumber());
        apiRequest.put("avatarUrl", newAvatarUrl);

        log.debug("Sending update request: {}", apiRequest);
        return executeRequest(HttpMethod.PUT, AUTH_API_URL + "/update-profile", apiRequest, createJsonHeaders(),
                AuthResponse.class, "Invalid input");
    }

    public AuthResponse refreshToken(String refreshToken) {
        log.info("Refreshing token");
        HttpHeaders headers = createBearerHeaders(refreshToken);
        return executeRequest(HttpMethod.POST, AUTH_API_URL + "/refresh", null, headers, AuthResponse.class,
                "Invalid token");
    }

    public AuthResponse updatePassword(String oldPassword, String newPassword) {
        Map<String, String> passwordRequest = new HashMap<>();
        passwordRequest.put("oldPassword", oldPassword);
        passwordRequest.put("newPassword", newPassword);

        return executeRequest(HttpMethod.PUT, AUTH_API_URL + "/update-password", passwordRequest, createJsonHeaders(),
                AuthResponse.class, "Invalid request");
    }

    // Centralized HTTP request execution
    private <T, R> R executeRequest(HttpMethod method, String endpoint, T body, HttpHeaders headers,
            Class<R> responseType,
            String defaultErrorMessage) {
        try {
            HttpEntity<T> entity = new HttpEntity<>(body, headers);
            ResponseEntity<R> response = restTemplate.exchange(BASE_API_URL + endpoint, method, entity, responseType);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new ApiException("Request failed", response.getStatusCode().value());
            }
            return response.getBody();
        } catch (ApiException e) {
            log.error("Request to {} failed: {}", endpoint, e.getMessage());
            throw handleHttpError(e, defaultErrorMessage);
        }
    }

    // Simplified POST method
    private <T> AuthResponse executePost(String endpoint, T request, Class<AuthResponse> responseType,
            String defaultErrorMessage) {
        try {
            return postToApi(endpoint, request, responseType);
        } catch (ApiException e) {
            log.error("POST to {} failed: {}", endpoint, e.getMessage());
            return handleApiError(e, defaultErrorMessage);
        }
    }

    // Centralized avatar management
    private String manageAvatar(MultipartFile file, String username, String oldAvatarUrl) throws IOException {
        if (file == null || file.isEmpty()) {
            if (oldAvatarUrl != null && !oldAvatarUrl.isEmpty()) {
                fileStorageService.deleteFile(oldAvatarUrl);
                log.info("Deleted old avatar: {}", oldAvatarUrl);
            }
            return null;
        }
        String newAvatarUrl = handleAvatarUpload(file, username);
        if (oldAvatarUrl != null && !oldAvatarUrl.isEmpty()) {
            fileStorageService.deleteFile(oldAvatarUrl);
            log.info("Deleted old avatar: {}", oldAvatarUrl);
        }
        return newAvatarUrl;
    }

    // Avatar upload helper
    private String handleAvatarUpload(MultipartFile file, String username) throws IOException {
        if (file != null && !file.isEmpty()) {
            String path = "user/" + username;
            String url = fileStorageService.storeFile(file, path);
            log.debug("Avatar uploaded: {}", url);
            return url;
        }
        return null;
    }

    // Centralized error handling for ApiException
    private AuthResponse handleApiError(ApiException e, String defaultMessage) {
        if (e.getCause() instanceof HttpClientErrorException) {
            HttpClientErrorException clientError = (HttpClientErrorException) e.getCause();
            AuthResponse errorResponse = parseErrorResponse(clientError);
            if (errorResponse != null && errorResponse.getMessage() != null) {
                return errorResponse;
            }
            String errorMessage = clientError.getResponseBodyAsString().isEmpty() ? defaultMessage
                    : clientError.getResponseBodyAsString();
            return AuthResponse.builder().message(errorMessage).build();
        }
        return AuthResponse.builder().message("Request failed: " + e.getMessage()).build();
    }

    // Centralized error handling for RuntimeException
    private RuntimeException handleHttpError(ApiException e, String defaultMessage) {
        if (e.getCause() instanceof HttpClientErrorException) {
            HttpClientErrorException clientError = (HttpClientErrorException) e.getCause();
            String errorMessage = clientError.getResponseBodyAsString().isEmpty() ? defaultMessage
                    : clientError.getResponseBodyAsString();
            return new RuntimeException(errorMessage);
        }
        return new RuntimeException("Request failed: " + e.getMessage());
    }

    // Header creation helpers
    private HttpHeaders createBearerHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    private HttpHeaders createJsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private AuthResponse parseErrorResponse(HttpClientErrorException e) {
        try {
            return objectMapper.readValue(e.getResponseBodyAsString(), AuthResponse.class);
        } catch (Exception ex) {
            log.warn("Failed to parse error response: {}", e.getResponseBodyAsString(), ex);
            return null;
        }
    }
}