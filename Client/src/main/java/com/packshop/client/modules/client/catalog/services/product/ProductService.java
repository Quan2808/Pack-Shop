package com.packshop.client.modules.client.catalog.services.product;

import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.packshop.client.common.services.CatalogBaseService;

public class ProductService extends CatalogBaseService {
    private static final String PRODUCTS_API_URL = "products";

    public ProductService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        super(restTemplate, objectMapper);
    }
}
