package com.packshop.client.modules.dashboard.catalog.services.category;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.packshop.client.dto.catalog.CategoryDTO;
import com.packshop.client.modules.dashboard.catalog.services.CatalogBaseService;

@Service
public class CategoryService extends CatalogBaseService {

    private static final String CATEGORIES_API_URL = "categories";

    public CategoryService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        super(restTemplate, objectMapper);
    }

    public List<CategoryDTO> getAllCategories() {
        return getAllFromApi(CATEGORIES_API_URL, CategoryDTO[].class);
    }

    public CategoryDTO getCategory(Long id) {
        return getFromApi(CATEGORIES_API_URL, id, CategoryDTO.class);
    }

    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        return postToApi(CATEGORIES_API_URL, categoryDTO, CategoryDTO.class);
    }

    public void updateCategory(Long id, CategoryDTO categoryDTO) {
        putToApi(CATEGORIES_API_URL, categoryDTO, id);
    }

    public String getCategoryNameById(Long categoryId) {
        if (categoryId == null)
            return null;
        try {
            CategoryDTO category = getFromApi(CATEGORIES_API_URL, categoryId, CategoryDTO.class);
            return category != null ? category.getName() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
