package com.packshop.client.services.catalog.product;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
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
        return getAllFromApi(PRODUCTS_API_URL, ProductDTO[].class);
    }

    public ProductDTO getProduct(Long id) {
        return getFromApi(PRODUCTS_API_URL, id, ProductDTO.class);
    }

    public ProductDTO createProduct(ProductDTO productDTO) throws IOException {
        log.info("Creating product: {}", productDTO);

        // Create a copy of productDTO without file-related fields for API communication
        ProductDTO apiProduct = new ProductDTO();
        BeanUtils.copyProperties(productDTO, apiProduct, "thumbnailFile", "mediaFiles");

        // Handle thumbnail upload
        apiProduct.setThumbnail(handleThumbnailUpload(productDTO.getThumbnailFile()));

        // Handle media files upload
        apiProduct.setMedia(handleMediaFilesUpload(productDTO.getMediaFiles()));

        ProductDTO createdProduct = postToApi(PRODUCTS_API_URL, apiProduct, ProductDTO.class);

        log.info("Product created successfully: {}", createdProduct);
        return createdProduct;
    }

    public void updateProduct(Long id, ProductDTO productDTO) throws IOException {
        // Get the existing product to check for files to delete
        ProductDTO existingProduct = getProduct(id);

        // Create a copy of productDTO without file-related fields for API communication
        ProductDTO apiProduct = new ProductDTO();
        BeanUtils.copyProperties(productDTO, apiProduct, "thumbnailFile", "mediaFiles");

        // Handle thumbnail update
        if (productDTO.getThumbnailFile() != null && !productDTO.getThumbnailFile().isEmpty()) {
            fileStorageService.deleteFile(existingProduct.getThumbnail());
            apiProduct.setThumbnail(handleThumbnailUpload(productDTO.getThumbnailFile()));
        } else {
            // Keep existing thumbnail if no new file is uploaded
            apiProduct.setThumbnail(existingProduct.getThumbnail());
        }

        // Handle media files update
        if (productDTO.getMediaFiles() != null && !productDTO.getMediaFiles().isEmpty()) {
            // Delete old media files if they exist
            fileStorageService.deleteFiles(existingProduct.getMedia());
            apiProduct.setMedia(handleMediaFilesUpload(productDTO.getMediaFiles()));
        } else {
            // Keep existing media files if no new files are uploaded
            apiProduct.setMedia(existingProduct.getMedia());
        }

        putToApi(PRODUCTS_API_URL, apiProduct, id);
        log.info("Product updated successfully: {}", apiProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        ProductDTO product = getProduct(id);

        // Delete the product from the API first
        try {
            restTemplate.delete(API_BASE_URL + "/{id}", id);
        } catch (Exception e) {
            log.error("Failed to delete product from API, skipping file deletion", e);
        }

        // After successful API deletion, delete associated files
        deleteProductFiles(product);
    }

    private void deleteProductFiles(ProductDTO product) {
        try {
            if (product.getThumbnail() != null) {
                fileStorageService.deleteFile(product.getThumbnail());
            }
            if (product.getMedia() != null) {
                fileStorageService.deleteFiles(product.getMedia());
            }
        } catch (Exception e) {
            log.error("Error deleting product files: {}", product.getId(), e);
        }
    }

    private String handleThumbnailUpload(MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            return fileStorageService.storeFile(file);
        }
        return null;
    }

    private List<String> handleMediaFilesUpload(List<MultipartFile> files) throws IOException {
        List<String> mediaPaths = new ArrayList<>();
        if (files != null && !files.isEmpty()) {
            for (MultipartFile mediaFile : files) {
                if (!mediaFile.isEmpty()) {
                    mediaPaths.add(fileStorageService.storeFile(mediaFile));
                }
            }
        }
        return mediaPaths;
    }

}
