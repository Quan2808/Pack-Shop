package com.packshop.client.services.catalog.product;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.packshop.client.common.exception.ProductNotFoundException;
import com.packshop.client.common.utilities.FileStorageService;
import com.packshop.client.dto.catalog.product.ProductDTO;
import com.packshop.client.services.catalog.base.CatalogBaseService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ProductService extends CatalogBaseService {

    private final String API_BASE_URL = "http://localhost:8080/api/catalog/products";
    private static final String PRODUCTS_API_URL = "products";
    private final FileStorageService fileStorageService;

    public ProductService(RestTemplate restTemplate, ObjectMapper objectMapper, FileStorageService fileStorageService) {
        super(restTemplate, objectMapper);
        this.fileStorageService = fileStorageService;
    }

    public List<ProductDTO> getAllProducts() {
        log.debug("Fetching all products from catalog");
        return getListFromApi(PRODUCTS_API_URL,
                new ParameterizedTypeReference<List<ProductDTO>>() {
                });
    }

    public ProductDTO createProduct(ProductDTO productDTO) throws IOException {
        log.info("Creating product: {}", productDTO);

        // Create a copy of productDTO without file-related fields for API communication
        ProductDTO apiProduct = new ProductDTO();
        BeanUtils.copyProperties(productDTO, apiProduct, "thumbnailFile", "mediaFiles");

        // Handle thumbnail upload
        if (productDTO.getThumbnailFile() != null && !productDTO.getThumbnailFile().isEmpty()) {
            String thumbnailPath = fileStorageService.storeFile(productDTO.getThumbnailFile());
            apiProduct.setThumbnail(thumbnailPath);
            log.info("Thumbnail uploaded: {}", thumbnailPath);
        }

        // Handle media files upload
        if (productDTO.getMediaFiles() != null && !productDTO.getMediaFiles().isEmpty()) {
            List<String> mediaPaths = new ArrayList<>();
            for (MultipartFile mediaFile : productDTO.getMediaFiles()) {
                if (!mediaFile.isEmpty()) {
                    String mediaPath = fileStorageService.storeFile(mediaFile);
                    mediaPaths.add(mediaPath);
                    log.info("Media file uploaded: {}", mediaPath);
                }
            }
            apiProduct.setMedia(mediaPaths);
        }

        // // Set headers for JSON content
        // HttpHeaders headers = new HttpHeaders();
        // headers.setContentType(MediaType.APPLICATION_JSON);

        // HttpEntity<ProductDTO> requestEntity = new HttpEntity<>(apiProduct, headers);

        // return restTemplate.postForObject("http://localhost:8080/api/catalog/" +
        // PRODUCTS_API_URL, requestEntity,
        // ProductDTO.class);

        ProductDTO createdProduct = postToApi(PRODUCTS_API_URL, apiProduct, ProductDTO.class);

        log.info("Product created successfully: {}", createdProduct);
        return createdProduct;
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

    public void updateProduct(Long id, ProductDTO productDTO) throws IOException {
        // Get the existing product to check for files to delete
        ProductDTO existingProduct = getProduct(id);

        // Create a copy of productDTO without file-related fields for API communication
        ProductDTO apiProduct = new ProductDTO();
        BeanUtils.copyProperties(productDTO, apiProduct, "thumbnailFile", "mediaFiles");

        // Handle thumbnail update
        if (productDTO.getThumbnailFile() != null && !productDTO.getThumbnailFile().isEmpty()) {
            // Delete old thumbnail if it exists
            if (existingProduct.getThumbnail() != null && !existingProduct.getThumbnail().isEmpty()) {
                fileStorageService.deleteFile(existingProduct.getThumbnail());
            }

            // Store new thumbnail
            String thumbnailPath = fileStorageService.storeFile(productDTO.getThumbnailFile());
            apiProduct.setThumbnail(thumbnailPath);
        } else {
            // Keep existing thumbnail if no new file is uploaded
            apiProduct.setThumbnail(existingProduct.getThumbnail());
        }

        // Handle media files update
        if (productDTO.getMediaFiles() != null && !productDTO.getMediaFiles().isEmpty()) {
            // Delete old media files if they exist
            if (existingProduct.getMedia() != null && !existingProduct.getMedia().isEmpty()) {
                fileStorageService.deleteFiles(existingProduct.getMedia());
            }

            // Store new media files
            List<String> mediaPaths = new ArrayList<>();
            for (MultipartFile mediaFile : productDTO.getMediaFiles()) {
                if (!mediaFile.isEmpty()) {
                    String mediaPath = fileStorageService.storeFile(mediaFile);
                    mediaPaths.add(mediaPath);
                }
            }
            apiProduct.setMedia(mediaPaths);
        } else {
            // Keep existing media files if no new files are uploaded
            apiProduct.setMedia(existingProduct.getMedia());
        }

        // Set headers for JSON content
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ProductDTO> requestEntity = new HttpEntity<>(apiProduct, headers);

        restTemplate.put(API_BASE_URL + "/{id}", requestEntity, id);
    }

    // @Transactional
    public void deleteProduct(Long id) {
        // First, get the product to retrieve file paths
        ProductDTO product = getProduct(id);

        // Delete the product from the API
        restTemplate.delete(API_BASE_URL + "/{id}", id);

        // After successful API deletion, delete associated files
        try {
            // Delete thumbnail
            if (product.getThumbnail() != null && !product.getThumbnail().isEmpty()) {
                fileStorageService.deleteFile(product.getThumbnail());
            }

            // Delete media files
            if (product.getMedia() != null && !product.getMedia().isEmpty()) {
                fileStorageService.deleteFiles(product.getMedia());
            }
        } catch (Exception e) {
            log.error("Error deleting product files for product ID: {}", id, e);
            // You might want to handle this error according to your requirements
            // For now, we'll log it but not throw an exception since the product is already
            // deleted
        }
    }

}
