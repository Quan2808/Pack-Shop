package com.packshop.client.controllers.dashboard;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.packshop.client.controllers.common.ViewRenderer;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {
    String BASE_PATH = "dashboard/pages";

    @RequestMapping
    public String home(Model model) {
        return ViewRenderer.renderView(model,
                BASE_PATH + "/home/index",
                "Overview");
    }
}