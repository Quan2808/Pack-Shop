package com.packshop.client.controllers.client;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.packshop.client.controllers.ViewRenderer;

@Controller
@RequestMapping("/account")
public class AccountController {

    @GetMapping("/authentication")
    public String authentication(Model model) {
        return ViewRenderer.renderView(model, "client/pages/account/authentication/index", "Authentication");
    }
}
