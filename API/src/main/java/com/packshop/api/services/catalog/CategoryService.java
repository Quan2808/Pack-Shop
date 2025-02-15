package com.packshop.api.services.catalog;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.packshop.api.common.exception.DuplicateResourceException;
import com.packshop.api.common.exception.ResourceNotFoundException;
import com.packshop.api.dto.catalog.CategoryDTO;
import com.packshop.api.entities.catalog.Category;
import com.packshop.api.repositories.catalog.CategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    public CategoryDTO createCategory(CategoryDTO categoryDTO) {

        if (categoryRepository.existsByNameIgnoreCase(categoryDTO.getName())) {
            throw new DuplicateResourceException(
                    String.format("Category with name '%s' already exists", categoryDTO.getName()));
        }

        Category category = modelMapper.map(categoryDTO, Category.class);
        Category savedCategory = categoryRepository.save(category);
        return modelMapper.map(savedCategory, CategoryDTO.class);
    }

    public CategoryDTO getCategory(Long id) {
        return categoryRepository.findById(id)
                .map(category -> modelMapper.map(category, CategoryDTO.class))
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Category with ID %d not found", id)));
    }

    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(category -> modelMapper.map(category, CategoryDTO.class))
                .collect(Collectors.toList());
    }

    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        modelMapper.map(categoryDTO, category);
        category.setId(id);

        Category updatedCategory = categoryRepository.save(category);
        return modelMapper.map(updatedCategory, CategoryDTO.class);
    }

    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found");
        }
        categoryRepository.deleteById(id);
    }
}
