package com.packshop.client.modules.dashboard.catalog.controllers;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.catalog.CatalogException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.packshop.client.common.utilities.ViewRenderer;
import com.packshop.client.dto.catalog.CategoryDTO;
import com.packshop.client.dto.catalog.ProductDTO;
import com.packshop.client.modules.dashboard.catalog.services.CategoryManageService;
import com.packshop.client.modules.dashboard.catalog.services.ProductManageService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequestMapping("/dashboard/catalog/products")
public class ProductManageController {
    @Autowired
    private ViewRenderer viewRenderer;
    
    private static final String CATALOG_PATH = "dashboard/catalog";
    private static final String REDIRECT_TO_LIST = "redirect:/dashboard/catalog/products";
    private static final String ERROR_CREATE_FAILED = "Failed to create product. Please try again.";
    private static final String ERROR_UPDATE_FAILED = "Failed to update product. Please try again.";
    private static final String ERROR_NOT_FOUND = "Product not found.";

    private final ProductManageService productService;
    private final CategoryManageService categoryService;

    public ProductManageController(ProductManageService productService, CategoryManageService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String list(Model model) {
        try {
            log.info("Fetching all products...");
            List<ProductDTO> products = productService.getAllProducts();

            Map<Long, String> categoryNames = new HashMap<>();
            for (ProductDTO product : products) {
                Long categoryId = product.getCategoryId();
                if (!categoryNames.containsKey(categoryId)) {
                    String categoryName = categoryService.getCategoryNameById(categoryId);
                    categoryNames.put(categoryId, categoryName != null ? categoryName : "Unknown Category");
                }
            }

            model.addAttribute("products", products);
            model.addAttribute("categoryNames", categoryNames);
        } catch (Exception e) {
            log.error("Error fetching products", e);
            model.addAttribute("errorMessage", "Failed to fetch products. Please try again.");
        }
        return viewRenderer.renderView(model, CATALOG_PATH + "/products/list/index", "Products List");
    }

    @GetMapping("/create")
    public String showCreateProductForm(Model model, RedirectAttributes redirectAttributes) {
        try {
            log.info("Fetching categories for create product form....");
            model.addAttribute("product", new ProductDTO());
            populateCategories(model);
            return viewRenderer.renderView(model, CATALOG_PATH + "/products/create/index", "Create Product");
        } catch (Exception e) {
            log.error("Error while showing create product form", e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Failed to load create product form. Redirecting to product list...");
            return REDIRECT_TO_LIST;
        }
    }

    @PostMapping("/create/submit")
    public String createProduct(@ModelAttribute("product") @Valid ProductDTO productDTO,
            BindingResult bindingResult,
            Model model) {
        log.info("Received request to create product: {}", productDTO);
        populateCategories(model);

        if (bindingResult.hasErrors()) {
            log.warn("Validation failed for product: {}", productDTO);
            model.addAttribute("errorMessage", "Failed to create product. Please fix the errors below.");
            return viewRenderer.renderView(model, CATALOG_PATH + "/products/create/index", "Create Product");
        }

        try {
            productService.createProduct(productDTO);
            log.info("Product created successfully: {}", productDTO.getName());
            return REDIRECT_TO_LIST;
        } catch (CatalogException e) {
            log.error("API error creating product with name: {}", productDTO.getName(), e);
            model.addAttribute("errorMessage", "API error: " + e.getMessage());
            return viewRenderer.renderView(model, CATALOG_PATH + "/products/create/index", "Create Product");
        } catch (IOException e) {
            log.error("File upload error creating product: {}", productDTO.getName(), e);
            model.addAttribute("errorMessage", "File upload error: " + e.getMessage());
            return viewRenderer.renderView(model, CATALOG_PATH + "/products/create/index", "Create Product");
        } catch (Exception e) {
            log.error("Unexpected error creating product: {}", productDTO.getName(), e);
            model.addAttribute("errorMessage", ERROR_CREATE_FAILED);
            return viewRenderer.renderView(model, CATALOG_PATH + "/products/create/index", "Create Product");
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        if (id == null || id <= 0) {
            log.warn("Invalid product ID: {}", id);
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid product ID.");
            return REDIRECT_TO_LIST;
        }
        try {
            ProductDTO product = productService.getProduct(id);

            if (product == null) {
                log.warn("Product not found with ID: {}", id);
                redirectAttributes.addFlashAttribute("errorMessage", ERROR_NOT_FOUND);
                return REDIRECT_TO_LIST;
            }

            model.addAttribute("product", product);
            populateCategories(model);

            return viewRenderer.renderView(model, CATALOG_PATH + "/products/edit/index", "Edit Product");
        } catch (Exception e) {
            log.error("Unexpected error fetching product for editing: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "An error occurred while fetching the product details. Please try again later.");
            return REDIRECT_TO_LIST;
        }
    }

    @PostMapping("/edit/{id}/submit")
    public String updateProduct(@PathVariable Long id,
            @ModelAttribute("product") @Valid ProductDTO productDTO,
            BindingResult bindingResult,
            Model model) {
        log.info("Received request to update product {}: {}", id, productDTO);

        if (bindingResult.hasErrors()) {
            log.error("Validation failed for product update: {}", productDTO);
            populateCategories(model);
            model.addAttribute("errorMessage", "Failed to update product. Please fix the errors below.");
            return viewRenderer.renderView(model, CATALOG_PATH + "/products/edit/index", "Edit Product");
        }

        try {

            productService.updateProduct(id, productDTO);
            log.info("Product updated successfully: {}", productDTO.getName());
            return REDIRECT_TO_LIST;
        } catch (Exception e) {
            log.error("Error updating product: {}", productDTO, e);
            List<CategoryDTO> categories = categoryService.getAllCategories();
            model.addAttribute("categories", categories);
            model.addAttribute("errorMessage", ERROR_UPDATE_FAILED);
            return viewRenderer.renderView(model, CATALOG_PATH + "/products/edit/index", "Edit Product");
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.info("Received request to delete product with ID: {}", id);

        try {
            ProductDTO product = productService.getProduct(id);
            if (product == null) {
                log.warn("Product not found with ID: {}", id);
                redirectAttributes.addFlashAttribute("errorMessage", ERROR_NOT_FOUND);
                return REDIRECT_TO_LIST;
            }
            productService.deleteProduct(id);
            log.info("Successfully deleted product with ID: {}", id);
            redirectAttributes.addFlashAttribute("successMessage", "Product has been deleted successfully!");
        } catch (Exception e) {
            log.error("Error deleting product with ID: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete product. Please try again.");
        }
        return REDIRECT_TO_LIST;
    }

    private void populateCategories(Model model) {
        try {
            List<CategoryDTO> categories = categoryService.getAllCategories();
            if (categories == null || categories.isEmpty()) {
                log.warn("No categories found");
                model.addAttribute("categories", Collections.emptyList());
            } else {
                model.addAttribute("categories", categories);
            }
        } catch (Exception e) {
            log.error("Error fetching categories", e);
            model.addAttribute("categories", Collections.emptyList());
            model.addAttribute("warningMessage", "Could not load categories.");
        }
    }
}