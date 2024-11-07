package com.packshop.client.controllers.dashboard.catalog.product;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.packshop.client.controllers.common.ViewRenderer;
import com.packshop.client.controllers.dashboard.catalog.base.CatalogManageBaseController;
import com.packshop.client.services.catalog.product.ProductService;

@Controller
public class ProductManageController implements CatalogManageBaseController {

    @Autowired
    private ProductService productService;

    @GetMapping("/products")
    public String list(Model model) {
        List<Map<String, Object>> products = productService.getProducts();
        model.addAttribute("products", products);

        return ViewRenderer.renderView(model,
                CATALOG_PATH + "/products/list/index",
                "Product List");
    }

    @GetMapping("/products/create")
    public String showCreateProductForm(Model model) {
        return ViewRenderer.renderView(model,
                CATALOG_PATH + "/products/create/index",
                "Create Product");
    }
}