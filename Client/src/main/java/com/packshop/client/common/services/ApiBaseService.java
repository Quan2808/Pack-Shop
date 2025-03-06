package com.packshop.client.common.services;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.packshop.client.common.exceptions.ApiException;

import lombok.extern.slf4j.Slf4j;
import com.packshop.client.common.exceptions.ErrorDetails;

@Slf4j
public abstract class ApiBaseService {
    protected static final String BASE_API_URL = "http://localhost:8080/api/";

    protected final RestTemplate restTemplate;
    protected final ObjectMapper objectMapper;

    public ApiBaseService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    protected <T> T getFromApi(String apiUrl, Long id, Class<T> responseType) {
        String url = BASE_API_URL + apiUrl + (id != null ? "/" + id : "");
        try {
            ResponseEntity<T> response = executeRequest(url, HttpMethod.GET, createJsonEntity(null), responseType);
            return response.getBody();
        } catch (ApiException e) {
            throw e; // Re-throw để xử lý ở tầng cao hơn nếu cần
        }
    }

    protected <T> List<T> getAllFromApi(String apiUrl, Class<T[]> clazz) {
        String url = BASE_API_URL + apiUrl;
        ResponseEntity<T[]> response = executeRequest(url, HttpMethod.GET, createJsonEntity(null), clazz);
        return response.getBody() != null ? Arrays.asList(response.getBody()) : Collections.emptyList();
    }

    protected <T> T postToApi(String apiUrl, Object request, Class<T> responseType) {
        String url = BASE_API_URL + apiUrl;
        ResponseEntity<T> response = executeRequest(url, HttpMethod.POST, createJsonEntity(request), responseType);
        return response.getBody();
    }

    protected void putToApi(String apiUrl, Object request, Long id) {
        String url = BASE_API_URL + apiUrl + (id != null ? "/" + id : "");
        executeRequest(url, HttpMethod.PUT, createJsonEntity(request), Void.class);
    }

    protected <T> T putToApi(String apiUrl, Object request, Class<T> responseType) {
        String url = BASE_API_URL + apiUrl;
        ResponseEntity<T> response = executeRequest(url, HttpMethod.PUT, createJsonEntity(request), responseType);
        return response.getBody();
    }

    protected <T> List<T> putToApiMultiple(String apiUrl, List<?> request, Class<T> responseType) {
        String url = BASE_API_URL + apiUrl;
        ResponseEntity<List<T>> response = executeRequest(url, HttpMethod.PUT, createJsonEntity(request),
                new ParameterizedTypeReference<List<T>>() {
                });
        return response.getBody() != null ? response.getBody() : Collections.emptyList();
    }

    protected void deleteFromApi(String apiUrl, Long id) {
        String url = BASE_API_URL + apiUrl + (id != null ? "/" + id : "");
        executeRequest(url, HttpMethod.DELETE, createJsonEntity(null), Void.class);
    }

    private <T> ResponseEntity<T> executeRequest(String url, HttpMethod method, HttpEntity<?> entity,
            Class<T> responseType) {
        try {
            ResponseEntity<T> response = restTemplate.exchange(url, method, entity, responseType);
            if (!response.getStatusCode().is2xxSuccessful()) {
                ErrorDetails errorDetails = parseErrorResponse(response);
                log.error("API error: {} for {} - {}", response.getStatusCode(), url, errorDetails.getMessage());
                throw new ApiException(errorDetails.getMessage(), response.getStatusCode().value(),
                        errorDetails.getErrorCode(), errorDetails.getErrors());
            }
            return response;
        } catch (HttpClientErrorException e) {
            ErrorDetails errorDetails = parseErrorBody(e);
            log.error("Client error: {} for {} - {}", e.getStatusCode(), url, errorDetails.getMessage());
            throw new ApiException(errorDetails.getMessage(), e.getStatusCode().value(),
                    errorDetails.getErrorCode(), errorDetails.getErrors(), e);
        } catch (Exception e) {
            log.error("Unexpected error for {}: {}", url, e.getMessage(), e);
            throw new ApiException("Unexpected error", 500, "UNKNOWN_ERROR", null, e);
        }
    }

    private <T> ResponseEntity<T> executeRequest(String url, HttpMethod method, HttpEntity<?> entity,
            ParameterizedTypeReference<T> responseType) {
        try {
            ResponseEntity<T> response = restTemplate.exchange(url, method, entity, responseType);
            if (!response.getStatusCode().is2xxSuccessful()) {
                ErrorDetails errorDetails = parseErrorResponse(response);
                log.error("API error: {} for {} - {}", response.getStatusCode(), url, errorDetails.getMessage());
                throw new ApiException(errorDetails.getMessage(), response.getStatusCode().value(),
                        errorDetails.getErrorCode(), errorDetails.getErrors());
            }
            return response;
        } catch (HttpClientErrorException e) {
            ErrorDetails errorDetails = parseErrorBody(e);
            log.error("Client error: {} for {} - {}", e.getStatusCode(), url, errorDetails.getMessage());
            throw new ApiException(errorDetails.getMessage(), e.getStatusCode().value(),
                    errorDetails.getErrorCode(), errorDetails.getErrors(), e);
        } catch (Exception e) {
            log.error("Unexpected error for {}: {}", url, e.getMessage(), e);
            throw new ApiException("Unexpected error", 500, "UNKNOWN_ERROR", null, e);
        }
    }

    private HttpEntity<Object> createJsonEntity(Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    private ErrorDetails parseErrorResponse(ResponseEntity<?> response) {
        try {
            return objectMapper.readValue(objectMapper.writeValueAsString(response.getBody()), ErrorDetails.class);
        } catch (Exception e) {
            log.warn("Failed to parse error response: {}", e.getMessage());
            return new ErrorDetails(response.getStatusCode().value(), "Unknown error", "UNKNOWN_ERROR", "unknown",
                    null);
        }
    }

    private ErrorDetails parseErrorBody(HttpClientErrorException e) {
        try {
            log.info("Error Body: {}", e.getResponseBodyAsString());
            return objectMapper.readValue(e.getResponseBodyAsString(), ErrorDetails.class);
        } catch (Exception ex) {
            log.warn("Failed to parse error body: {}", ex.getMessage());
            return new ErrorDetails(e.getStatusCode().value(), e.getStatusText(), "UNKNOWN_ERROR", "unknown", null);
        }
    }
}