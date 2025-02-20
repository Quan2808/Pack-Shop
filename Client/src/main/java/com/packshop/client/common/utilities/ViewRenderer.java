package com.packshop.client.common.utilities;

import org.springframework.ui.Model;

public class ViewRenderer {

    private static final String CLIENT_LAYOUT = "fragments/client/_layout";
    private static final String DASHBOARD_LAYOUT = "fragments/dashboard/_layout";

    public static String renderView(Model model, String templateName, String title) {
        model.addAttribute("view", templateName);
        model.addAttribute("title", title);

        if (templateName.contains("dashboard")) {
            return DASHBOARD_LAYOUT;
        }
        return CLIENT_LAYOUT;
    }
}
