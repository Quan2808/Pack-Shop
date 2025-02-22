package com.packshop.client.modules.client.home.controllers;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.packshop.client.common.services.AuthService;
import com.packshop.client.common.utilities.ViewRenderer;
import com.packshop.client.dto.identity.AuthResponse;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private ViewRenderer viewRenderer;

    private final AuthService authService;

    public AccountController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/authentication")
    public String authentication(Model model) {
        return viewRenderer.renderView(model, "client/account/authentication/index", "Authentication");
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password,
            HttpSession session, RedirectAttributes redirectAttributes) {
        log.debug("Login attempt for user: {}", username);
        try {
            AuthResponse response = authService.login(username, password);
            session.setAttribute("token", response.getToken());
            session.setAttribute("refreshToken", response.getRefreshToken());
            session.setAttribute("username", response.getUsername());
            session.setAttribute("roles", response.getRoles());

            Set<String> roles = response.getRoles();
            if (roles != null && response.getRoles().contains("ADMIN")) {
                session.setAttribute("isAdmin", true);
            }

            log.info("Login successful for user: {}", response);
            log.info("User role: {}", response.getRoles());
            log.info("Token for user {}: {}", response.getUsername(), response.getToken());

            redirectAttributes.addFlashAttribute("successMessage", "You have been successfully logged in.");
            return "redirect:/";
        } catch (Exception e) {
            String refreshToken = (String) session.getAttribute("refreshToken");
            if (refreshToken != null) {
                try {
                    AuthResponse refreshResponse = authService.refreshToken(refreshToken);
                    session.setAttribute("token", refreshResponse.getToken());
                    log.info("Token refreshed for user: {}", username);
                    return "redirect:/";
                } catch (Exception refreshEx) {
                    log.warn("Refresh token failed: {}", refreshEx.getMessage());
                }
            }
            log.warn("Login failed for user: {} - {}", username, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid username or password");
            return "redirect:/account/authentication";
        }
    }

    @PostMapping("/register")
    public String register(@RequestParam String username, @RequestParam String email,
            @RequestParam String password, RedirectAttributes redirectAttributes) {
        log.debug("Register attempt for user: {}", username);
        try {
            authService.register(username, email, password);
            log.info("User registered: {}", username);
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful. Please login.");
            return "redirect:/account/authentication";
        } catch (Exception e) {
            log.warn("Registration failed for user: {} - {}", username, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Registration failed: " + e.getMessage());
            return "redirect:/account/authentication";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        log.info("User logged out");
        session.invalidate();
        return "redirect:/account/authentication";
    }
}
