package com.packshop.client.services.catalog.product;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.packshop.client.dto.catalog.product.ProductDTO;
import com.packshop.client.services.catalog.base.CatalogBaseService;

@Service
public class ProductService extends CatalogBaseService {

    private static final String PRODUCTS_API_URL = "products";

    public ProductService(RestTemplate restTemplate) {
        super(restTemplate);
    }

    public List<ProductDTO> getAllProducts() {
        return getListFromApi(PRODUCTS_API_URL, new ParameterizedTypeReference<List<ProductDTO>>() {
        });
    }
}
