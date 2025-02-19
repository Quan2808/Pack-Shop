package com.packshop.client.modules.dashboard.catalog.controllers.product;

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

import com.packshop.client.common.utilities.ViewRenderer;
import com.packshop.client.dto.catalog.CategoryDTO;
import com.packshop.client.dto.catalog.ProductDTO;
import com.packshop.client.modules.dashboard.catalog.services.category.CategoryService;
import com.packshop.client.modules.dashboard.catalog.services.product.ProductService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequestMapping("/dashboard/catalog/products")
public class ProductManageController {

    private static final String CATALOG_PATH = "dashboard/pages/catalog";

    private final ProductService productService;
    private final CategoryService categoryService;

    public ProductManageController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping()
    public String list(Model model) {
        try {
            log.info("Fetching all products...");
            List<ProductDTO> products = productService.getAllProducts();
            model.addAttribute("products", products);
        } catch (Exception e) {
            log.error("Error fetching products", e);
            model.addAttribute("errorMessage", "Failed to fetch products. Please try again.");
        }

        return ViewRenderer.renderView(model,
                CATALOG_PATH + "/products/list/index",
                "Product List");
    }

    @GetMapping("/create")
    public String showCreateProductForm(Model model) {
        List<CategoryDTO> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("product", new ProductDTO());

        return ViewRenderer.renderView(model,
                CATALOG_PATH + "/products/create/index",
                "Create Product");
    }

    @PostMapping("/create/submit")
    public String createProduct(@ModelAttribute("product") @Valid ProductDTO productDTO,
            BindingResult bindingResult, Model model) {
        log.info("Received request to create product: {}", productDTO);

        if (bindingResult.hasErrors()) {
            log.error("Validation failed for product: {}", productDTO);

            List<CategoryDTO> categories = categoryService.getAllCategories();
            model.addAttribute("categories", categories);
            model.addAttribute("errorMessage", "Failed to create product. Please fix the errors below.");
            return ViewRenderer.renderView(model,
                    CATALOG_PATH + "/products/create/index",
                    "Create Product");
        }

        try {
            productService.createProduct(productDTO);
            log.info("Product created successfully: {}", productDTO.getName());
            return "redirect:/dashboard/catalog/products";
        } catch (Exception e) {
            log.error("Error creating product: {}", productDTO, e);

            List<CategoryDTO> categories = categoryService.getAllCategories();
            model.addAttribute("categories", categories);
            model.addAttribute("errorMessage", "Failed to create product. Please try again.");
            return ViewRenderer.renderView(model,
                    CATALOG_PATH + "/products/create/index",
                    "Create Product");
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            ProductDTO product = productService.getProduct(id);
            List<CategoryDTO> categories = categoryService.getAllCategories();

            model.addAttribute("product", product);
            model.addAttribute("categories", categories);

            return ViewRenderer.renderView(model,
                    CATALOG_PATH + "/products/edit/index",
                    "Edit Product");
        } catch (Exception e) {
            log.error("Error fetching product for editing: {}", id, e);
            model.addAttribute("errorMessage",
                    "An error occurred while fetching the product details for editing. Please try again later.");
            // return "redirect:/dashboard/catalog/products";
            return list(model);
        }
    }

    @PostMapping("/edit/{id}/submit")
    public String updateProduct(@PathVariable Long id,
            @ModelAttribute("product") @Valid ProductDTO productDTO,
            BindingResult bindingResult, Model model) {
        log.info("Received request to update product {}: {}", id, productDTO);

        if (bindingResult.hasErrors()) {
            log.error("Validation failed for product update: {}", productDTO);

            List<CategoryDTO> categories = categoryService.getAllCategories();
            model.addAttribute("categories", categories);
            model.addAttribute("errorMessage", "Failed to update product. Please fix the errors below.");

            return ViewRenderer.renderView(model,
                    CATALOG_PATH + "/products/edit/index",
                    "Edit Product");
        }

        try {
            productService.updateProduct(id, productDTO);
            log.info("Product updated successfully: {}", productDTO.getName());
            return "redirect:/dashboard/catalog/products";
        } catch (Exception e) {
            log.error("Error updating product: {}", productDTO, e);

            List<CategoryDTO> categories = categoryService.getAllCategories();
            model.addAttribute("categories", categories);
            model.addAttribute("errorMessage", "Failed to update product. Please try again.");

            return ViewRenderer.renderView(model,
                    CATALOG_PATH + "/products/edit/index",
                    "Edit Product");
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.info("Received request to delete product with ID: {}", id);

        try {
            productService.deleteProduct(id);
            log.info("Successfully deleted product with ID: {}", id);
            redirectAttributes.addFlashAttribute("successMessage", "Product has been deleted successfully!");
        } catch (Exception e) {
            log.error("Error deleting product with ID: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete product. Please try again.");
        }

        return "redirect:/dashboard/catalog/products";
    }

}