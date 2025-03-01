package com.packshop.client.modules.dashboard.catalog.controllers;

import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.packshop.client.common.exceptions.ApiException;
import com.packshop.client.common.utilities.ViewRenderer;
import com.packshop.client.dto.catalog.CategoryDTO;
import com.packshop.client.modules.dashboard.catalog.services.CategoryManageService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequestMapping("/dashboard/catalog/categories")
public class CategoryManageController {


    private static final String CATALOG_PATH = "dashboard/catalog";
    private static final String REDIRECT_TO_LIST = "redirect:/dashboard/catalog/categories";
    private static final String ERROR_CREATE_FAILED =
            "Failed to create category. Please try again.";
    private static final String ERROR_UPDATE_FAILED =
            "Failed to update category. Please try again.";
    private static final String ERROR_NOT_FOUND = "Category not found.";

    private final CategoryManageService categoryService;
    private final ObjectMapper objectMapper;
    private final ViewRenderer viewRenderer;

    public CategoryManageController(CategoryManageService categoryService,
            ObjectMapper objectMapper, ViewRenderer viewRenderer) {
        this.categoryService = categoryService;
        this.objectMapper = objectMapper;
        this.viewRenderer = viewRenderer;
    }

    @GetMapping
    public String list(Model model) {
        try {
            log.info("Fetching all categories...");
            List<CategoryDTO> categories = categoryService.getAllCategories();
            model.addAttribute("categories", categories);

            String categoriesJson = objectMapper.writeValueAsString(categories);
            model.addAttribute("categoriesData", categoriesJson);
            model.addAttribute("category", new CategoryDTO());
        } catch (Exception e) {
            log.error("Error fetching categories", e);
            model.addAttribute("errorMessage", "Failed to fetch categories. Please try again.");
        }
        return viewRenderer.renderView(model, CATALOG_PATH + "/categories/list/index",
                "Categories List");
    }

    @GetMapping("/create")
    public String showCreateCategoryForm(Model model) {
        log.info("Showing create category form...");
        model.addAttribute("category", new CategoryDTO());
        return viewRenderer.renderView(model, CATALOG_PATH + "/categories/create/index",
                "Create Category");
    }

    @PostMapping("/create/submit")
    public String createCategory(@ModelAttribute("category") @Valid CategoryDTO categoryDTO,
            BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        log.info("Received request to create category: {}", categoryDTO);

        if (bindingResult.hasErrors()) {
            log.warn("Validation failed for category: {}", categoryDTO);
            model.addAttribute("errorMessage",
                    "Failed to create category. Please fix the errors below.");
            return viewRenderer.renderView(model, CATALOG_PATH + "/categories/create/index",
                    "Create Category");
        }

        try {
            categoryService.createCategory(categoryDTO);
            log.info("Category created successfully: {}", categoryDTO.getName());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Category created successfully!");
            return REDIRECT_TO_LIST;
        } catch (ApiException e) {
            if (e.getStatusCode() == 409) {
                log.error("Category name already exists: {}", categoryDTO.getName(), e);
                // model.addAttribute("errorMessage", "Category name '" + categoryDTO.getName()
                // + "' already exists.");
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Category name '" + categoryDTO.getName() + "' already exists.");
            } else {
                log.error("Failed to create category: {}", categoryDTO.getName(), e);
                redirectAttributes.addFlashAttribute("errorMessage", ERROR_CREATE_FAILED);
            }
            return REDIRECT_TO_LIST;
        } catch (Exception e) {
            log.error("Unexpected error creating category: {}", categoryDTO.getName(), e);
            model.addAttribute("errorMessage", ERROR_CREATE_FAILED);
            return viewRenderer.renderView(model, CATALOG_PATH + "/categories/create/index",
                    "Create Category");
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model,
            RedirectAttributes redirectAttributes) {
        if (id == null || id <= 0) {
            log.warn("Invalid category ID: {}", id);
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid category ID.");
            return REDIRECT_TO_LIST;
        }

        try {
            CategoryDTO category = categoryService.getCategory(id);
            if (category == null) {
                log.warn("Category not found with ID: {}", id);
                redirectAttributes.addFlashAttribute("errorMessage", ERROR_NOT_FOUND);
                return REDIRECT_TO_LIST;
            }
            model.addAttribute("category", category);
            return viewRenderer.renderView(model, CATALOG_PATH + "/categories/edit/index",
                    "Edit Category");
        } catch (Exception e) {
            log.error("Error fetching category with ID: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "An error occurred while fetching the category details. Please try again later.");
            return REDIRECT_TO_LIST;
        }
    }

    @PostMapping("/edit/submit")
    public String updateCategory(@ModelAttribute("category") @Valid CategoryDTO categoryDTO,
            BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (categoryDTO.getId() == null || categoryDTO.getId() <= 0) {
            log.warn("Invalid category ID: {}", categoryDTO.getId());
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid category ID.");
            return REDIRECT_TO_LIST;
        }

        log.info("Received request to update category {}: {}", categoryDTO.getId(), categoryDTO);

        if (bindingResult.hasErrors()) {
            log.warn("Validation failed for category update: {}", categoryDTO);
            model.addAttribute("errorMessage",
                    "Failed to update category. Please fix the errors below.");
            return viewRenderer.renderView(model, CATALOG_PATH + "/categories/edit/index",
                    "Edit Category");
        }

        try {
            categoryService.updateCategory(categoryDTO.getId(), categoryDTO);
            log.info("Category updated successfully: {}", categoryDTO.getName());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Category updated successfully!");
            return REDIRECT_TO_LIST;
        } catch (Exception e) {
            log.error("Error updating category: {}", categoryDTO.getName(), e);
            model.addAttribute("errorMessage", ERROR_UPDATE_FAILED);
            return viewRenderer.renderView(model, CATALOG_PATH + "/categories/edit/index",
                    "Edit Category");
        }
    }

    // @GetMapping("/delete/{id}")
    // public String deleteCategory(@PathVariable Long id, RedirectAttributes
    // redirectAttributes) {
    // if (id == null || id <= 0) {
    // log.warn("Invalid category ID: {}", id);
    // redirectAttributes.addFlashAttribute("errorMessage", "Invalid category ID.");
    // return REDIRECT_TO_LIST;
    // }

    // log.info("Received request to delete category with ID: {}", id);

    // try {
    // CategoryDTO category = categoryService.getCategory(id);
    // if (category == null) {
    // log.warn("Category not found with ID: {}", id);
    // redirectAttributes.addFlashAttribute("errorMessage", ERROR_NOT_FOUND);
    // return REDIRECT_TO_LIST;
    // }
    // categoryService.deleteCategory(id);
    // log.info("Category deleted successfully with ID: {}", id);
    // redirectAttributes.addFlashAttribute("successMessage", "Category deleted
    // successfully!");
    // } catch (Exception e) {
    // log.error("Error deleting category with ID: {}", id, e);
    // redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete
    // category. Please try again.");
    // }
    // return REDIRECT_TO_LIST;
    // }
}
