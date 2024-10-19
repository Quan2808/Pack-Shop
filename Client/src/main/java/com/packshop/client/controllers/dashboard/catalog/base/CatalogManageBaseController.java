package com.packshop.client.controllers.dashboard.catalog.base;

import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/dashboard/catalog") // định nghĩa rõ là path
public interface CatalogManageBaseController {
    String CATALOG_PATH = "dashboard/pages/catalog";
}