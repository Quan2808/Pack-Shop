package com.packshop.client.modules.dashboard.home.controllers;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public String listAccounts(Model model) {
        try {
            List<UserResponse> accounts = service.getAllUsers();
            model.addAttribute("accounts", accounts);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to fetch Users. Please try again.");
        }
        return viewRenderer.renderView(model, ACCOUNT_PATH + "/list/index", "Accounts List");
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            UserResponse account = service.getProduct(id);
            log.info("Fetched User: {}", account);
            model.addAttribute("userInfo", account);
            return viewRenderer.renderView(model, ACCOUNT_PATH + "/detail/index", "Account Detais");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to fetch User. Please try again.");
            return "redirect:/dashboard/account";
        }
    }

}
