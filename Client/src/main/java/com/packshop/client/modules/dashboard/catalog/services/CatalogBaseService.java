package com.packshop.client.modules.dashboard.catalog.services;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.packshop.client.common.exceptions.CatalogException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class CatalogBaseService {
    protected static final String CATALOG_API_URL = "http://localhost:8080/api/catalog/";

    protected final RestTemplate restTemplate;
    protected final ObjectMapper objectMapper;

    public CatalogBaseService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    protected <T> T getFromApi(String apiUrl, Long id, Class<T> responseType) {
        try {
            ResponseEntity<T> response = restTemplate.getForEntity(
                    CATALOG_API_URL + apiUrl + "/" + id, responseType);
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("API returned non-success status: {} for {}/{}", response.getStatusCode(), apiUrl, id);
                throw new CatalogException("API error: " + response.getStatusCode().value());
            }
            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.error("Client error from API {}/{}: {}", apiUrl, id, e.getStatusCode());
            throw new CatalogException("Client error: " + e.getStatusText(), e);
        } catch (Exception e) {
            log.error("Failed to fetch data from API {}/{}", apiUrl, id, e);
            throw new CatalogException("Failed to fetch data with id: " + id, e);
        }
    }

    protected <T> List<T> getAllFromApi(String apiUrl, Class<T[]> clazz) {
        try {
            ResponseEntity<T[]> response = restTemplate.exchange(
                    CATALOG_API_URL + apiUrl, HttpMethod.GET, null, clazz);
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("API returned non-success status: {} for {}", response.getStatusCode(), apiUrl);
                throw new CatalogException("API error: " + response.getStatusCode().value());
            }
            return response.getBody() != null ? Arrays.asList(response.getBody()) : Collections.emptyList();
        } catch (HttpClientErrorException e) {
            log.error("Client error from API {}: {}", apiUrl, e.getStatusCode());
            throw new CatalogException("Client error: " + e.getStatusText(), e);
        } catch (Exception e) {
            log.error("Failed to fetch data from API {}", apiUrl, e);
            throw new CatalogException("Failed to fetch data", e);
        }
    }

    protected <T> T postToApi(String apiUrl, Object request, Class<T> responseType) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Object> entity = new HttpEntity<>(request, headers);

            ResponseEntity<T> response = restTemplate.exchange(
                    CATALOG_API_URL + apiUrl,
                    HttpMethod.POST,
                    entity,
                    responseType);

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("API returned non-success status: {} for POST to {}", response.getStatusCode(), apiUrl);
                throw new CatalogException("API error", response.getStatusCode().value());
            }
            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.error("Client error from API {}: {}", apiUrl, e.getStatusCode());
            throw new CatalogException("Client error: " + e.getStatusText(), e.getStatusCode().value(), e);
        } catch (Exception e) {
            log.error("Unexpected error when posting to API {}: {}", apiUrl, e.getMessage(), e);
            throw new CatalogException("An unexpected error occurred", e);
        }
    }

    protected <T> void putToApi(String apiUrl, Object request, Long id) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Object> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Void> response = restTemplate.exchange(
                    CATALOG_API_URL + apiUrl + "/" + id,
                    HttpMethod.PUT,
                    entity,
                    Void.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("API returned non-success status: {} for PUT to {}/{}",
                        response.getStatusCode(), apiUrl, id);
                throw new CatalogException("API error: " + response.getStatusCode().value());
            }
        } catch (HttpClientErrorException e) {
            log.error("Client error from API {}/{}: {}", apiUrl, id, e.getStatusCode());
            throw new CatalogException("Client error: " + e.getStatusText(), e);
        } catch (Exception e) {
            log.error("Failed to update data at API {}/{}: {}", apiUrl, id, e.getMessage(), e);
            throw new CatalogException("Failed to update data with id: " + id, e);
        }
    }

    protected void deleteFromApi(String apiUrl, Long id) {
        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                    CATALOG_API_URL + apiUrl + "/" + id,
                    HttpMethod.DELETE,
                    null,
                    Void.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("API returned non-success status: {} for DELETE to {}/{}",
                        response.getStatusCode(), apiUrl, id);
                throw new CatalogException("API error: " + response.getStatusCode().value());
            }
        } catch (HttpClientErrorException e) {
            log.error("Client error from API {}/{}: {}", apiUrl, id, e.getStatusCode());
            throw new CatalogException("Client error: " + e.getStatusText(), e);
        } catch (Exception e) {
            log.error("Failed to delete data from API {}/{}: {}", apiUrl, id, e.getMessage(), e);
            throw new CatalogException("Failed to delete data with id: " + id, e);
        }
    }
}