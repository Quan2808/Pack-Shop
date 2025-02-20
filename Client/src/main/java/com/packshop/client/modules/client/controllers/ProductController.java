package com.packshop.client.modules.client.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.packshop.client.common.utilities.ViewRenderer;

@Controller
@RequestMapping("/products")
public class ProductController {

    @GetMapping
    public String products(Model model) {
        return ViewRenderer.renderView(model, "client/products/categories/index", "All Products");
    }

    // Get by category
    @GetMapping("/{category}")
    public String productsByCategory(Model model) {
        return ViewRenderer.renderView(model, "client/products/categories/index", "All Products");
    }

    // get by product
    @GetMapping("/{product}")
    public String productsByProduct(Model model) {
        return ViewRenderer.renderView(model, "client/products/categories/index", "Product Name");
    }

    @GetMapping("/example")
    public String exp(Model model) {
        return ViewRenderer.renderView(model, "client/products/item/index", "Product Name");
    }
}
