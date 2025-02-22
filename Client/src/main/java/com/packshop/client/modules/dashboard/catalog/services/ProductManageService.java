package com.packshop.client.modules.dashboard.catalog.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.packshop.client.common.services.CatalogBaseService;
import com.packshop.client.common.utilities.FileStorageService;
import com.packshop.client.dto.catalog.ProductDTO;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ProductManageService extends CatalogBaseService {

    private static final String PRODUCTS_API_URL = "products";
    private final FileStorageService fileStorageService;
    private final CategoryManageService categoryService;

    public ProductManageService(RestTemplate restTemplate, ObjectMapper objectMapper,
            FileStorageService fileStorageService,
            CategoryManageService categoryService) {
        super(restTemplate, objectMapper);
        this.fileStorageService = fileStorageService;
        this.categoryService = categoryService;
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
        return getFromApi(PRODUCTS_API_URL, id, ProductDTO.class);
    }

    public ProductDTO createProduct(ProductDTO productDTO) throws IOException {
        log.info("Creating product: {}", productDTO);

        // Create a copy of productDTO without file-related fields for API communication
        ProductDTO apiProduct = new ProductDTO();
        BeanUtils.copyProperties(productDTO, apiProduct, "thumbnailFile", "mediaFiles");

        String categoryName = categoryService.getCategoryNameById(productDTO.getCategoryId());

        apiProduct
                .setThumbnail(handleThumbnailUpload(productDTO.getThumbnailFile(), categoryName, productDTO.getName()));
        apiProduct.setMedia(handleMediaFilesUpload(productDTO.getMediaFiles(), categoryName, productDTO.getName()));

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

        String categoryName = categoryService.getCategoryNameById(productDTO.getCategoryId());

        // Handle thumbnail update
        if (productDTO.getThumbnailFile() != null && !productDTO.getThumbnailFile().isEmpty()) {
            fileStorageService.deleteFile(existingProduct.getThumbnail());
            apiProduct.setThumbnail(
                    handleThumbnailUpload(productDTO.getThumbnailFile(), categoryName, productDTO.getName()));
        } else {
            // Keep existing thumbnail if no new file is uploaded
            apiProduct.setThumbnail(existingProduct.getThumbnail());
        }

        // Handle media files update
        if (productDTO.getMediaFiles() != null && !productDTO.getMediaFiles().isEmpty()) {
            // Check if there are any non-empty files in the list
            boolean hasNonEmptyFiles = productDTO.getMediaFiles().stream()
                    .anyMatch(file -> file != null && !file.isEmpty());

            if (hasNonEmptyFiles) {
                // Only delete old media files if new ones are being uploaded
                fileStorageService.deleteFiles(existingProduct.getMedia());
                apiProduct.setMedia(
                        handleMediaFilesUpload(productDTO.getMediaFiles(), categoryName, productDTO.getName()));
            } else {
                // If no new files are being uploaded, keep the existing media
                apiProduct.setMedia(productDTO.getMedia());
            }
        } else {
            // If mediaFiles is null or empty, preserve the existing media paths
            apiProduct.setMedia(productDTO.getMedia());
        }

        putToApi(PRODUCTS_API_URL, apiProduct, id);
        log.info("Product updated successfully: {}", apiProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        log.info("Deleting product with ID: {}", id);
        ProductDTO product = getProduct(id);

        if (product == null) {
            log.warn("Product not found with ID: {}", id);
            return;
        }

        try {
            deleteFromApi(PRODUCTS_API_URL, id);
            deleteProductFiles(product);
            log.info("Product deleted successfully: {}", id);
        } catch (Exception e) {
            log.error("Failed to delete product with ID: {}", id, e);
            throw e;
        }
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

    private String handleThumbnailUpload(MultipartFile file, String categoryName, String productName)
            throws IOException {
        if (file != null && !file.isEmpty()) {
            return fileStorageService.storeFile(file, categoryName, productName, true);
        }
        return null;
    }

    private List<String> handleMediaFilesUpload(List<MultipartFile> files, String categoryName, String productName)
            throws IOException {
        List<String> mediaPaths = new ArrayList<>();
        if (files != null) {
            for (MultipartFile mediaFile : files) {
                if (mediaFile != null && !mediaFile.isEmpty()) {
                    mediaPaths.add(fileStorageService.storeFile(mediaFile, categoryName, productName, false));
                }
            }
        }
        return mediaPaths;
    }

}
