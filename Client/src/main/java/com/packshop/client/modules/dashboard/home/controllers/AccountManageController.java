package com.packshop.client.modules.dashboard.home.controllers;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.packshop.client.common.utilities.ViewRenderer;
import com.packshop.client.dto.identity.UserResponse;
import com.packshop.client.modules.dashboard.home.services.AccountManageService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/dashboard/account")
public class AccountManageController {

    private static final String ACCOUNT_PATH = "dashboard/account";
    private final ViewRenderer viewRenderer;
    private final AccountManageService service;

    public AccountManageController(ViewRenderer viewRenderer, AccountManageService accountManageService) {
        this.viewRenderer = viewRenderer;
        this.service = accountManageService;
    }

    @RequestMapping
    public String home(Model model) {
        try {
            log.info("Fetching all accounts...");
            List<UserResponse> accounts = service.getAllUsers();
            log.info("Account response {}:", accounts);
            model.addAttribute("accounts", accounts);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to fetch Users. Please try again.");
        }
        return viewRenderer.renderView(model, ACCOUNT_PATH + "/index", "Customers List");
    }

}
