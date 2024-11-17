package com.packshop.api.services.catalog.category;

import com.packshop.api.dto.catalog.category.CategoryDTO;
import com.packshop.api.entities.catalog.category.Category;
import com.packshop.api.exception.ResourceNotFoundException;
import com.packshop.api.repositories.catalog.category.CategoryRepository;
import com.packshop.api.utilities.MapperUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryDTO> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return MapperUtil.mapEntitiesToDtos(categories, CategoryDTO.class);
    }

    public CategoryDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return MapperUtil.mapToDto(category, CategoryDTO.class);
    }

    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        Category category = MapperUtil.mapToEntity(categoryDTO, Category.class);
        category = categoryRepository.save(category);
        return MapperUtil.mapToDto(category, CategoryDTO.class);
    }

    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        MapperUtil.updateEntityFromDto(category, categoryDTO);
        category = categoryRepository.save(category);
        return MapperUtil.mapToDto(category, CategoryDTO.class);
    }

    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found with id: " + id);
        }
        categoryRepository.deleteById(id);
    }
}

