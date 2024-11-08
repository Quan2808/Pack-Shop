package com.packshop.client.services.catalog.product;

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.packshop.client.dto.catalog.product.ProductDTO;
import com.packshop.client.services.catalog.base.CatalogBaseService;

@Service
public class ProductService implements CatalogBaseService {

    private static final String PRODUCTS_API_URL = CATALOG_API_URL + "products";

    private final RestTemplate restTemplate;

    public ProductService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<ProductDTO> getAllProducts() {
        ResponseEntity<List<ProductDTO>> response = restTemplate.exchange(
                PRODUCTS_API_URL,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<ProductDTO>>() {
                });
        return response.getBody() != null ? response.getBody() : new ArrayList<>();
    }

}
