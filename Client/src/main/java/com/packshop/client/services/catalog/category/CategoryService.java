package com.packshop.client.services.catalog.category;

import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.packshop.client.services.catalog.base.CatalogBaseService;

@Service
public class CategoryService implements CatalogBaseService {

    private static final String CATEGORIES_API_URL = CATALOG_API_URL + "categories";

    private final RestTemplate restTemplate;

    public CategoryService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<Map<String, Object>> getCategories() {
        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                CATEGORIES_API_URL, HttpMethod.GET,
                null, new ParameterizedTypeReference<List<Map<String, Object>>>() {
                });
        return response.getBody();
    }
}
