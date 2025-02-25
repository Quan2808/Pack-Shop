package com.packshop.client.modules.dashboard.home.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import com.packshop.client.common.utilities.ViewRenderer;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    String BASE_PATH = "dashboard";

    private final ViewRenderer viewRenderer;

    DashboardController(ViewRenderer viewRenderer) {
        this.viewRenderer = viewRenderer;
    }

    @RequestMapping
    public String home(Model model) {
        return viewRenderer.renderView(model, BASE_PATH + "/home/index", "Overview");
    }
}
