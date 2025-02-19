package com.packshop.client.modules.dashboard.catalog.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.catalog.CatalogException;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
            return restTemplate.getForObject(
                    CATALOG_API_URL + apiUrl + "/" + id,
                    responseType);
        } catch (Exception e) {
            log.error("Failed to fetch data from API {}/{}", apiUrl, id, e);
            throw new CatalogException("Failed to fetch data with id: " + id, e);
        }
    }

    protected <T> List<T> getAllFromApi(String apiUrl, Class<T[]> clazz) {
        try {
            ResponseEntity<T[]> response = restTemplate.exchange(
                    CATALOG_API_URL + apiUrl,
                    HttpMethod.GET,
                    null,
                    clazz);

            return response.getBody() != null ? Arrays.asList(response.getBody()) : new ArrayList<>();

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

            return response.getBody();

        } catch (Exception e) {
            log.error("Unexpected error when posting to API {}: {}", apiUrl, e.getMessage());
            throw new CatalogException("An unexpected error occurred", e);
        }
    }

    protected <T> void putToApi(String apiUrl, Object request, Long id) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Object> entity = new HttpEntity<>(request, headers);
            restTemplate.put(CATALOG_API_URL + apiUrl + "/" + id, entity);
        } catch (Exception e) {
            log.error("Failed to update data at API {}", apiUrl, e);
            throw new CatalogException("Failed to update data", e);
        }
    }

}