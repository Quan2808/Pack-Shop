package com.packshop.client.modules.dashboard.home.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.packshop.client.common.utilities.ViewRenderer;

@Controller
@RequestMapping("/dashboard/account")
public class AccountManageController {

    private static final String ACCOUNT_PATH = "dashboard/account";
    private final ViewRenderer viewRenderer;

    public AccountManageController(ViewRenderer viewRenderer) {
        this.viewRenderer = viewRenderer;
    }

    @RequestMapping
    public String home(Model model) {
        return viewRenderer.renderView(model, ACCOUNT_PATH + "/index", "Customers List");
    }

}
