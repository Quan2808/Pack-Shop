package com.packshop.client.services.catalog.product;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.packshop.client.dto.catalog.product.ProductDTO;
import com.packshop.client.services.catalog.base.CatalogBaseService;

@Service
public class ProductService extends CatalogBaseService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    private static final String PRODUCTS_API_URL = "products";

    public ProductService(RestTemplate restTemplate) {
        super(restTemplate);
    }

    public List<ProductDTO> getAllProducts() {
        return getListFromApi(PRODUCTS_API_URL, new ParameterizedTypeReference<List<ProductDTO>>() {
        });
    }

    public void createProduct(ProductDTO productDTO) {
        try {
            // Log thông tin sản phẩm trước khi tạo
            logger.info("Attempting to create product with SKU: {}", productDTO.getSku());
            // Gửi yêu cầu POST đến API để tạo sản phẩm mới
            restTemplate.postForObject(CATALOG_API_URL + PRODUCTS_API_URL, productDTO, ProductDTO.class);
            logger.info("Successfully created product with SKU: {}", productDTO.getSku());
        } catch (Exception e) {
            // Log lỗi nếu có khi gọi API
            logger.error("Failed to create product with SKU: {}", productDTO.getSku(), e);
            throw new RuntimeException("Failed to create product", e);
        }
    }
}
