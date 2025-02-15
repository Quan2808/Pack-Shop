package com.packshop.client.services.catalog.product;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.packshop.client.common.exception.ProductCreationException;
import com.packshop.client.common.exception.ProductNotFoundException;
import com.packshop.client.common.utilities.FileService;
import com.packshop.client.dto.catalog.product.ProductDTO;
import com.packshop.client.services.catalog.base.CatalogBaseService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ProductService extends CatalogBaseService {

    private static final String PRODUCTS_API_URL = "products";
    private final FileService fileService;

    public ProductService(RestTemplate restTemplate, ObjectMapper objectMapper, FileService fileService) {
        super(restTemplate, objectMapper);
        this.fileService = fileService;
    }

    public List<ProductDTO> getAllProducts() {
        log.debug("Fetching all products from catalog");
        return getListFromApi(PRODUCTS_API_URL,
                new ParameterizedTypeReference<List<ProductDTO>>() {
                });
    }

    public ProductDTO createProduct(ProductDTO createDTO) {
        try {
            log.info("Creating new product with SKU: {}", createDTO.getSku());

            // Handle file uploads
            String thumbnailPath = fileService.saveFile(createDTO.getThumbnailFile());
            List<String> mediaPaths = fileService.saveFiles(createDTO.getMediaFiles());

            // Create API request DTO
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("name", createDTO.getName());
            requestMap.put("thumbnail", thumbnailPath);
            requestMap.put("status", createDTO.getStatus());
            requestMap.put("description", createDTO.getDescription());
            requestMap.put("media", mediaPaths);
            requestMap.put("price", createDTO.getPrice());
            requestMap.put("sku", createDTO.getSku());
            requestMap.put("quantity", createDTO.getQuantity());
            requestMap.put("categoryId", createDTO.getCategoryId());
            requestMap.put("material", createDTO.getMaterial());
            requestMap.put("dimensions", createDTO.getDimensions());
            requestMap.put("capacity", createDTO.getCapacity());
            requestMap.put("weight", createDTO.getWeight());

            return postToApi(PRODUCTS_API_URL, requestMap, ProductDTO.class);

        } catch (Exception e) {
            log.error("Failed to create product with SKU: {}", createDTO.getSku(), e);
            throw new ProductCreationException("Failed to create product: " + e.getMessage(), e);
        }

        // catch (IOException e) {
        // log.error("Failed to handle file upload for product with SKU: {}",
        // createDTO.getSku(), e);
        // throw new FileUploadException("Failed to upload files: " + e.getMessage(),
        // e);
        // }
    }

    public ProductDTO getProduct(Long id) {
        try {
            return restTemplate.getForObject(
                    CATALOG_API_URL + PRODUCTS_API_URL + "/" + id,
                    ProductDTO.class);
        } catch (Exception e) {
            log.error("Failed to fetch product with id: {}", id, e);
            throw new ProductNotFoundException("Product not found with id: " + id);
        }
    }
}
