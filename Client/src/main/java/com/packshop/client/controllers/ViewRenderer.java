package com.packshop.client.controllers;

import org.springframework.ui.Model;

public class ViewRenderer {

    private static final String CLIENT_LAYOUT = "client/fragments/_layout";
    private static final String DASHBOARD_LAYOUT = "dashboard/fragments/_layout";

    public static String renderView(Model model, String templateName, String title) {
        model.addAttribute("view", templateName);
        model.addAttribute("title", title);

        if (templateName.contains("dashboard")) {
            return DASHBOARD_LAYOUT;
        }
        return CLIENT_LAYOUT;

    }
}
