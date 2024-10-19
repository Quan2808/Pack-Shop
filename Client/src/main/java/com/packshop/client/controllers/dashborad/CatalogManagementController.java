package com.packshop.client.controllers.dashborad;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.packshop.client.controllers.ViewRenderer;

@Controller
@RequestMapping("dashboard/catalog")
public class CatalogManagementController {

    // Product Management
    @GetMapping("/products")
    public String home(Model model) {
        return ViewRenderer.renderView(model, "dashboard/pages/catalog/products/list/index", "Product List");
    }

}
