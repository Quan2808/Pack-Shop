package com.packshop.api.services.catalog;

import com.packshop.api.dto.catalog.CategoryDTO;
import com.packshop.api.etities.catalog.Category;
import com.packshop.api.exception.ResourceNotFoundException;
import com.packshop.api.repositories.catalog.CategoryRepository;
import com.packshop.api.utilities.EntityDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryDTO> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return EntityDtoMapper.convertListToDto(categories, CategoryDTO.class);
    }

    public CategoryDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return EntityDtoMapper.mapToDto(category, CategoryDTO.class);
    }

    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        Category category = EntityDtoMapper.mapToEntity(categoryDTO, Category.class);
        category = categoryRepository.save(category);
        return EntityDtoMapper.mapToDto(category, CategoryDTO.class);
    }

    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        EntityDtoMapper.updateEntityFromDto(category, categoryDTO);
        category = categoryRepository.save(category);
        return EntityDtoMapper.mapToDto(category, CategoryDTO.class);
    }

    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found with id: " + id);
        }
        categoryRepository.deleteById(id);
    }
}

