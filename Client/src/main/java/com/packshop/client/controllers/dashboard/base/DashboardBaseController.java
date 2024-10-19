package com.packshop.client.controllers.dashboard.base;

import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/dashboard")
public interface DashboardBaseController {
    String BASE_PATH = "dashboard/pages";
}