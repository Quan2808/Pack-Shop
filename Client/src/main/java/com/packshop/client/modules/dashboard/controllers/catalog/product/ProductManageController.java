package com.packshop.client.modules.dashboard.controllers.catalog.product;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.packshop.client.common.utilities.ViewRenderer;
import com.packshop.client.dto.catalog.CategoryDTO;
import com.packshop.client.dto.catalog.ProductDTO;
import com.packshop.client.modules.dashboard.services.catalog.category.CategoryService;
import com.packshop.client.modules.dashboard.services.catalog.product.ProductService;

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
        List<ProductDTO> products = productService.getAllProducts();
        model.addAttribute("products", products);

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
            model.addAttribute("error", "Failed to create product. Please fix the errors below.");
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
            model.addAttribute("error", "Failed to create product. Please try again.");
            return ViewRenderer.renderView(model,
                    CATALOG_PATH + "/products/create/index",
                    "Create Product");
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        log.info("Received request to delete product with ID: {}", id);

        try {
            productService.deleteProduct(id);
            log.info("Successfully deleted product with ID: {}", id);
        } catch (Exception e) {
            log.error("Error deleting product with ID: {}", id, e);
        }

        return "redirect:/dashboard/catalog/products";
    }

}