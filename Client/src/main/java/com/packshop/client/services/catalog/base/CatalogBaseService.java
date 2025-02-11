package com.packshop.client.services.catalog.base;

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public abstract class CatalogBaseService {
    protected static final String CATALOG_API_URL = "http://localhost:8080/api/catalog/";

    // Make restTemplate protected so it can be accessed by subclasses
    protected final RestTemplate restTemplate;

    public CatalogBaseService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    protected <T> List<T> getListFromApi(String apiUrl, ParameterizedTypeReference<List<T>> responseType) {
        // try {
        // } catch (Exception e) {
        // e.printStackTrace();
        // return new ArrayList<>();
        // }
        ResponseEntity<List<T>> response = restTemplate.exchange(
                CATALOG_API_URL + apiUrl,
                HttpMethod.GET,
                null,
                responseType);
        return response.getBody() != null ? response.getBody() : new ArrayList<>();
    }
}