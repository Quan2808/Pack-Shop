package com.packshop.client.controllers.client;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.packshop.client.controllers.ViewRenderer;

@Controller
@RequestMapping("/products")
public class ProductController {

    @GetMapping
    public String products(Model model) {
        return ViewRenderer.renderView(model, "client/pages/products/categories/index", "All Products");
    }
}
