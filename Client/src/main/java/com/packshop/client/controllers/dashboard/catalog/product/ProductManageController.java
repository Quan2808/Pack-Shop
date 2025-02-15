package com.packshop.client.controllers.dashboard.catalog.product;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.packshop.client.controllers.common.ViewRenderer;
import com.packshop.client.controllers.dashboard.catalog.base.CatalogManageBaseController;
import com.packshop.client.dto.catalog.category.CategoryDTO;
import com.packshop.client.dto.catalog.product.ProductDTO;
import com.packshop.client.services.catalog.category.CategoryService;
import com.packshop.client.services.catalog.product.ProductService;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class ProductManageController implements CatalogManageBaseController {

    private final ProductService productService;
    private final CategoryService categoryService;

    public ProductManageController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping("/products")
    public String list(Model model) {
        List<ProductDTO> products = productService.getAllProducts();
        model.addAttribute("products", products);

        return ViewRenderer.renderView(model,
                CATALOG_PATH + "/products/list/index",
                "Product List");
    }

    @GetMapping("/products/create")
    public String showCreateProductForm(Model model) {
        List<CategoryDTO> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("product", new ProductDTO());

        return ViewRenderer.renderView(model,
                CATALOG_PATH + "/products/create/index",
                "Create Product");
    }

    @PostMapping("/products/create")
    public String createProduct(@ModelAttribute("product") ProductDTO productDTO, Model model) {
        log.info("Received request to create product: {}", productDTO);

        try {
            productService.createProduct(productDTO);
            log.info("Product created successfully: {}", productDTO.getName());

            return "redirect:/dashboard/catalog/products";
        } catch (Exception e) {
            log.error("Error creating product: {}", productDTO, e);

            // Xử lý lỗi khi tạo sản phẩm
            model.addAttribute("error", "Failed to create product. Please try again.");
            model.addAttribute("product", productDTO);
            return ViewRenderer.renderView(model,
                    CATALOG_PATH + "/products/create/index",
                    "Create Product");
        }
    }

    @GetMapping("/products/delete/{id}")
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