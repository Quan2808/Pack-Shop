package com.packshop.client.modules.client.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.packshop.client.common.utilities.ViewRenderer;

@Controller
@RequestMapping("/account")
public class AccountController {

    @GetMapping("/authentication")
    public String authentication(Model model) {
        return ViewRenderer.renderView(model, "client/account/authentication/index", "Authentication");
    }
}
