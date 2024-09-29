package com.packshop.client.controllers;

import org.springframework.ui.Model;

public class ViewRenderer {

    private static final String LAYOUT = "client/fragments/_layout";

    public static String renderView(Model model, String templateName, String title) {
        model.addAttribute("view", templateName);
        model.addAttribute("title", title);
        return LAYOUT;
    }
}
