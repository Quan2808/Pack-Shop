package com.packshop.client.controllers.dashborad;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.packshop.client.controllers.ViewRenderer;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    @RequestMapping
    public String dashboard(Model model) {
        return ViewRenderer.renderView(model, "dashboard/pages/dashboard/index", "Home");
    }
}
