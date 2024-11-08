package com.packshop.client.services.catalog.category;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.packshop.client.dto.catalog.category.CategoryDTO;
import com.packshop.client.services.catalog.base.CatalogBaseService;

@Service
public class CategoryService extends CatalogBaseService {

    private static final String CATEGORIES_API_URL = "categories";

    public CategoryService(RestTemplate restTemplate) {
        super(restTemplate);
    }

    public List<CategoryDTO> getAllCategories() {
        return getListFromApi(CATEGORIES_API_URL, new ParameterizedTypeReference<List<CategoryDTO>>() {
        });
    }
}
