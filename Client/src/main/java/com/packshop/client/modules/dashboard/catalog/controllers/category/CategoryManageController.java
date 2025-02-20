package com.packshop.client.modules.dashboard.catalog.controllers.category;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.packshop.client.common.utilities.ViewRenderer;
import com.packshop.client.dto.catalog.CategoryDTO;
import com.packshop.client.modules.dashboard.catalog.services.category.CategoryService;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequestMapping("/dashboard/catalog/categories")
public class CategoryManageController {

    private static final String CATALOG_PATH = "dashboard/pages/catalog";

    private final CategoryService categoryService;

    public CategoryManageController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping()
    public String list(Model model) {
        try {
            log.info("Fetching all categories...");
            List<CategoryDTO> categories = categoryService.getAllCategories();
            model.addAttribute("categories", categories);

            ObjectMapper objectMapper = new ObjectMapper();
            String messagesJson = objectMapper.writeValueAsString(categories);

            model.addAttribute("categoriesData", messagesJson);
        } catch (Exception e) {
            log.error("Error fetching categories", e);
            model.addAttribute("errorMessage", "Failed to fetch categories. Please try again.");
        }

        return ViewRenderer.renderView(model,
                CATALOG_PATH + "/categories/list/index",
                "Categories List");
    }
}