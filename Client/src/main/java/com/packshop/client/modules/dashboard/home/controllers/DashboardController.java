package com.packshop.client.modules.dashboard.home.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.packshop.client.common.utilities.ViewRenderer;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {
    @Autowired
    private ViewRenderer viewRenderer;

    String BASE_PATH = "dashboard";

    @RequestMapping
    public String home(Model model) {
        return viewRenderer.renderView(model,
                BASE_PATH + "/home/index",
                "Overview");
    }
}