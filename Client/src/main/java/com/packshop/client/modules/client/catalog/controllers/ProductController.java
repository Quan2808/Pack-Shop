package com.packshop.client.modules.client.catalog.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.packshop.client.common.utilities.ViewRenderer;
import com.packshop.client.dto.catalog.ProductDTO;
import com.packshop.client.modules.client.catalog.services.product.ProductService;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequestMapping("/products")
public class ProductController {
    @Autowired
    private ViewRenderer viewRenderer;

    private final ProductService productService;
    private static final String REDIRECT_TO_LIST = "redirect:/products";
    private static final String ERROR_NOT_FOUND = "Product not found.";

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public String getAllProducts(Model model) {
        try {
            log.info("Fetching all products...");
            List<ProductDTO> products = productService.getAllProducts();
            model.addAttribute("products", products);
        } catch (Exception e) {
            log.error("Error fetching products", e);
            model.addAttribute("errorMessage", "Failed to fetch products. Please try again.");
        }
        return viewRenderer.renderView(model, "client/products/categories/index", "All Products");
    }

    // Get by category
    // @GetMapping("/{category}")
    // public String productsByCategory(Model model) {
    // return viewRenderer.renderView(model, "client/products/categories/index",
    // "All Products");
    // }

    @GetMapping("/{id}")
    public String getProduct(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            log.info("Fetching product with ID: {}", id);
            ProductDTO product = productService.getProduct(id);
            model.addAttribute("product", product);
            return viewRenderer.renderView(model, "client/products/item/index", product.getName()); //
        } catch (Exception e) {
            log.error("Error fetching product with ID: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", ERROR_NOT_FOUND);
            return REDIRECT_TO_LIST;
        }
    }

    @GetMapping("/example")
    public String exp(Model model) {
        return viewRenderer.renderView(model, "client/products/item/index", "Product Name");
    }
}
