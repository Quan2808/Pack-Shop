package com.packshop.client.modules.dashboard.catalog.services.category;

import java.util.List;

import javax.xml.catalog.CatalogException;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.packshop.client.dto.catalog.CategoryDTO;
import com.packshop.client.modules.dashboard.catalog.services.CatalogBaseService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@CacheConfig(cacheNames = "categories")
public class CategoryManageService extends CatalogBaseService {

    private static final String CATEGORIES_API_URL = "categories";

    public CategoryManageService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        super(restTemplate, objectMapper);
    }

    @Cacheable
    public List<CategoryDTO> getAllCategories() {
        log.debug("Fetching all categories from catalog API");
        try {
            return getAllFromApi(CATEGORIES_API_URL, CategoryDTO[].class);
        } catch (CatalogException e) {
            log.error("Failed to fetch all categories: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Cacheable(key = "#id")
    public CategoryDTO getCategory(Long id) {
        if (id == null || id <= 0) {
            log.warn("Invalid category ID: {}", id);
            throw new IllegalArgumentException("Category ID must be a positive number");
        }
        log.debug("Fetching category with ID: {}", id);
        try {
            CategoryDTO category = getFromApi(CATEGORIES_API_URL, id, CategoryDTO.class);
            if (category == null) {
                log.warn("Category not found with ID: {}", id);
            }
            return category;
        } catch (CatalogException e) {
            log.error("Failed to fetch category with ID: {}", id, e);
            throw e;
        }
    }

    @CacheEvict(cacheNames = "categories", allEntries = true)
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        if (categoryDTO == null || categoryDTO.getName() == null) {
            log.warn("Invalid category data for creation: {}", categoryDTO);
            throw new IllegalArgumentException("Category data and name must not be null");
        }
        log.info("Creating category: {}", categoryDTO.getName());
        try {
            CategoryDTO createdCategory = postToApi(CATEGORIES_API_URL, categoryDTO, CategoryDTO.class);
            log.info("Category created successfully: {}", createdCategory.getName());
            return createdCategory;
        } catch (CatalogException e) {
            log.error("Failed to create category: {}", categoryDTO.getName(), e);
            throw e; // Ném lại với statusCode
        }
    }

    @CacheEvict(cacheNames = "categories", allEntries = true)
    public void updateCategory(Long id, CategoryDTO categoryDTO) {
        if (id == null || id <= 0) {
            log.warn("Invalid category ID for update: {}", id);
            throw new IllegalArgumentException("Category ID must be a positive number");
        }
        if (categoryDTO == null || categoryDTO.getName() == null) {
            log.warn("Invalid category data for update: {}", categoryDTO);
            throw new IllegalArgumentException("Category data and name must not be null");
        }
        log.info("Updating category with ID: {}", id);
        try {
            putToApi(CATEGORIES_API_URL, categoryDTO, id);
            log.info("Category updated successfully: {}", categoryDTO.getName());
        } catch (CatalogException e) {
            log.error("Failed to update category with ID: {}", id, e);
            throw e;
        }
    }

    @Cacheable(key = "#categoryId")
    public String getCategoryNameById(Long categoryId) {
        if (categoryId == null || categoryId <= 0) {
            log.warn("Invalid category ID for name lookup: {}", categoryId);
            return null;
        }
        log.debug("Fetching category name for ID: {}", categoryId);
        try {
            CategoryDTO category = getFromApi(CATEGORIES_API_URL, categoryId, CategoryDTO.class);
            if (category == null) {
                log.warn("Category not found with ID: {}", categoryId);
                return null;
            }
            return category.getName();
        } catch (CatalogException e) {
            log.error("Failed to fetch category name for ID: {}", categoryId, e);
            return null;
        }
    }
}