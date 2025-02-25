package com.packshop.client.common.services;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
        try {
            ResponseEntity<T> response =
                    restTemplate.getForEntity(BASE_API_URL + apiUrl + "/" + id, responseType);
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("API error: {} for {}/{}", response.getStatusCode(), apiUrl, id);
                throw new ApiException("API error: " + response.getStatusCode().value());
            }
            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.error("Client error: {} for {}/{}", e.getStatusCode(), apiUrl, id);
            throw new ApiException("Client error: " + e.getStatusText(), e);
        } catch (Exception e) {
            log.error("Failed to fetch: {}/{}", apiUrl, id, e);
            throw new ApiException("Failed to fetch data with id: " + id, e);
        }
    }

    protected <T> List<T> getAllFromApi(String apiUrl, Class<T[]> clazz) {
        try {
            ResponseEntity<T[]> response =
                    restTemplate.exchange(BASE_API_URL + apiUrl, HttpMethod.GET, null, clazz);
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("API error: {} for {}", response.getStatusCode(), apiUrl);
                throw new ApiException("API error: " + response.getStatusCode().value());
            }
            return response.getBody() != null ? Arrays.asList(response.getBody())
                    : Collections.emptyList();
        } catch (HttpClientErrorException e) {
            log.error("Client error: {} for {}", e.getStatusCode(), apiUrl);
            throw new ApiException("Client error: " + e.getStatusText(), e);
        } catch (Exception e) {
            log.error("Failed to fetch: {}", apiUrl, e);
            throw new ApiException("Failed to fetch data", e);
        }
    }

    protected <T> T postToApi(String apiUrl, Object request, Class<T> responseType) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Object> entity = new HttpEntity<>(request, headers);

            ResponseEntity<T> response = restTemplate.exchange(BASE_API_URL + apiUrl,
                    HttpMethod.POST, entity, responseType);

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("API error: {} for POST to {}", response.getStatusCode(), apiUrl);
                throw new ApiException("API error: " + response.getStatusCode().value());
            }
            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.error("Client error: {} for {}", e.getStatusCode(), apiUrl);
            throw new ApiException("Client error: " + e.getStatusText(), e);
        } catch (Exception e) {
            log.error("Unexpected error for POST to {}: {}", apiUrl, e.getMessage(), e);
            throw new ApiException("Unexpected error", e);
        }
    }

    protected void putToApi(String apiUrl, Object request, Long id) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Object> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Void> response = restTemplate.exchange(BASE_API_URL + apiUrl + "/" + id,
                    HttpMethod.PUT, entity, Void.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("API error: {} for PUT to {}/{}", response.getStatusCode(), apiUrl, id);
                throw new ApiException("API error: " + response.getStatusCode().value());
            }
        } catch (HttpClientErrorException e) {
            log.error("Client error: {} for {}/{}", e.getStatusCode(), apiUrl, id);
            throw new ApiException("Client error: " + e.getStatusText(), e);
        } catch (Exception e) {
            log.error("Failed to update: {}/{}", apiUrl, id, e);
            throw new ApiException("Failed to update data with id: " + id, e);
        }
    }

    protected void deleteFromApi(String apiUrl, Long id) {
        try {
            ResponseEntity<Void> response = restTemplate.exchange(BASE_API_URL + apiUrl + "/" + id,
                    HttpMethod.DELETE, null, Void.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("API error: {} for DELETE to {}/{}", response.getStatusCode(), apiUrl,
                        id);
                throw new ApiException("API error: " + response.getStatusCode().value());
            }
        } catch (HttpClientErrorException e) {
            log.error("Client error: {} for {}/{}", e.getStatusCode(), apiUrl, id);
            throw new ApiException("Client error: " + e.getStatusText(), e);
        } catch (Exception e) {
            log.error("Failed to delete: {}/{}", apiUrl, id, e);
            throw new ApiException("Failed to delete data with id: " + id, e);
        }
    }
}
