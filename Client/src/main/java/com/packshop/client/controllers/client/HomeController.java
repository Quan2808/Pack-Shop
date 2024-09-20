package com.packshop.client.controllers.client;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class HomeController {

    private static final String LAYOUT = "client/fragments/_layout";

    public static String renderView(Model model, String viewName, String pageTitle) {
        model.addAttribute("view", viewName);
        model.addAttribute("title", pageTitle);
        return LAYOUT;
    }

    @GetMapping()
    public String home(Model model) {
        return renderView(model, "client/home.html", "Home");
    }
    
}
