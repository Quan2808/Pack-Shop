package com.packshop.client.services.catalog.category;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.packshop.client.dto.catalog.category.CategoryDTO;
import com.packshop.client.services.catalog.base.CatalogBaseService;

@Service
public class CategoryService extends CatalogBaseService {

    private static final String CATEGORIES_API_URL = "categories";

    public CategoryService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        super(restTemplate, objectMapper);
    }

    public List<CategoryDTO> getAllCategories() {
        return getAllFromApi(CATEGORIES_API_URL, CategoryDTO[].class);
    }

    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        return postToApi(CATEGORIES_API_URL, categoryDTO, CategoryDTO.class);
    }

    public void updateCategory(Long id, CategoryDTO categoryDTO) {
        putToApi(CATEGORIES_API_URL, categoryDTO, id);
    }
}
