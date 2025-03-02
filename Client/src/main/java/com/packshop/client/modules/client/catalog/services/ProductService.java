package com.packshop.client.modules.client.catalog.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.packshop.client.common.services.ApiBaseService;
import com.packshop.client.dto.catalog.ProductDTO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProductService extends ApiBaseService {
    private static final String PRODUCTS_API_URL = "catalog/products";

    public ProductService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        super(restTemplate, objectMapper);
    }

    public List<ProductDTO> getAllProducts() {
        log.debug("Fetching all products from catalog");
        try {
            return getAllFromApi(PRODUCTS_API_URL, ProductDTO[].class);
        } catch (Exception e) {
            log.error("Failed to fetch all products: {}", e.getMessage(), e);
            throw e;
        }
    }

    public ProductDTO getProduct(Long id) {
        log.debug("Fetching product with ID: {}", id);

        try {
            return getFromApi(PRODUCTS_API_URL, id, ProductDTO.class);
        } catch (Exception e) {
            log.error("Failed to fetch product with ID: {}", id, e);
            throw e;
        }
    }
}
