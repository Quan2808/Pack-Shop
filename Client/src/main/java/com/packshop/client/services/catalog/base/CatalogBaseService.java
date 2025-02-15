package com.packshop.client.services.catalog.base;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.xml.catalog.CatalogException;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.packshop.client.common.exception.CatalogClientException;
import com.packshop.client.common.exception.CatalogServerException;

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

    protected <T> List<T> getListFromApi(String apiUrl, ParameterizedTypeReference<List<T>> responseType) {
        try {
            ResponseEntity<List<T>> response = restTemplate.exchange(
                    CATALOG_API_URL + apiUrl,
                    HttpMethod.GET,
                    null,
                    responseType);

            return Optional.ofNullable(response.getBody())
                    .orElse(new ArrayList<>());

        } catch (HttpClientErrorException e) {
            log.error("Client error when calling API {}: {}", apiUrl, e.getMessage());
            throw new CatalogClientException("Failed to fetch data from catalog", e);
        } catch (HttpServerErrorException e) {
            log.error("Server error when calling API {}: {}", apiUrl, e.getMessage());
            throw new CatalogServerException("Catalog service is currently unavailable", e);
        } catch (Exception e) {
            log.error("Unexpected error when calling API {}: {}", apiUrl, e.getMessage());
            throw new CatalogException("An unexpected error occurred", e);
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

        } catch (HttpClientErrorException e) {
            log.error("Client error when posting to API {}: {}", apiUrl, e.getMessage());
            throw new CatalogClientException("Failed to send data to catalog", e);
        } catch (HttpServerErrorException e) {
            log.error("Server error when posting to API {}: {}", apiUrl, e.getMessage());
            throw new CatalogServerException("Catalog service is currently unavailable", e);
        } catch (Exception e) {
            log.error("Unexpected error when posting to API {}: {}", apiUrl, e.getMessage());
            throw new CatalogException("An unexpected error occurred", e);
        }
    }
}