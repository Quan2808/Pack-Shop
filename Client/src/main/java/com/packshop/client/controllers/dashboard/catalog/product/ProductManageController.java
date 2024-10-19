package com.packshop.client.controllers.dashboard.catalog.product;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.packshop.client.controllers.common.ViewRenderer;
import com.packshop.client.controllers.dashboard.catalog.base.CatalogManageBaseController;

@Controller
public class ProductManageController implements CatalogManageBaseController {

    @GetMapping("/products")
    public String list(Model model) {
        return ViewRenderer.renderView(model,
                CATALOG_PATH + "/products/list/index",
                "Product List");
    }
}