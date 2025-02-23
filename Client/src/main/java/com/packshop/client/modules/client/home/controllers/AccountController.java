package com.packshop.client.modules.client.home.controllers;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.packshop.client.common.services.AuthService;
import com.packshop.client.common.utilities.ViewRenderer;
import com.packshop.client.dto.identity.AuthRegisterRequest;
import com.packshop.client.dto.identity.AuthRequest;
import com.packshop.client.dto.identity.AuthResponse;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/account")
@Slf4j
public class AccountController {

    @Autowired
    private ViewRenderer viewRenderer;

    private final AuthService authService;

    public AccountController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/authentication")
    public String authentication(Model model) {
        model.addAttribute("loginRequest", new AuthRequest());
        model.addAttribute("registerRequest", new AuthRegisterRequest());
        return viewRenderer.renderView(model, "client/account/authentication/index", "Authentication");
    }

    @PostMapping("/login")
    public String login(@ModelAttribute("loginRequest") @Valid AuthRequest request, BindingResult result,
            HttpSession session, RedirectAttributes redirectAttributes) {
        log.debug("Login attempt for user: {}", request.getUsername());

        if (result.hasErrors()) {
            log.warn("Validation failed for login request: {}", request);
            String errorMessage = result.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .reduce((msg1, msg2) -> msg1 + "; " + msg2)
                    .orElse("Validation failed");
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            return "redirect:/account/authentication";
        }

        AuthResponse response = authService.login(request.getUsername(), request.getPassword());
        log.debug("Login response message: {}", response.getMessage());

        if ("Login successful".equals(response.getMessage())) {
            session.setAttribute("token", response.getToken());
            session.setAttribute("refreshToken", response.getRefreshToken());
            session.setAttribute("username", response.getUsername());
            session.setAttribute("fullName", response.getFullName());
            session.setAttribute("roles", response.getRoles());

            Set<String> roles = response.getRoles();
            if (roles != null && roles.contains("ADMIN")) {
                session.setAttribute("isAdmin", true);
            }
            log.info("Login successful for user: {}", request.getUsername());
            redirectAttributes.addFlashAttribute("successMessage", response.getMessage());
            return "redirect:/";
        } else {
            log.warn("Login failed for user: {} - {}", request.getUsername(), response.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", response.getMessage());
            return "redirect:/account/authentication";
        }
    }

    @PostMapping("/register")
    public String register(@RequestParam String username, @RequestParam String email,
            @RequestParam String password, @RequestParam String fullName,
            RedirectAttributes redirectAttributes) {
        log.debug("Register attempt for user: {}", username);
        try {
            authService.register(username, email, password, fullName);
            log.info("User registered: {}", username);
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful. Please login.");
            return "redirect:/account/authentication";
        } catch (Exception e) {
            log.warn("Registration failed for user: {} - {}", username, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/account/authentication";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        log.info("User logged out: {}", session.getAttribute("username"));
        session.invalidate();
        redirectAttributes.addFlashAttribute("successMessage", "You have been logged out.");
        return "redirect:/account/authentication";
    }

    @PostMapping("/refresh")
    public String refreshToken(HttpSession session, RedirectAttributes redirectAttributes) {
        String refreshToken = (String) session.getAttribute("refreshToken");
        if (refreshToken == null) {
            log.warn("No refresh token found in session");
            redirectAttributes.addFlashAttribute("errorMessage", "Please login again.");
            return "redirect:/account/authentication";
        }

        try {
            AuthResponse response = authService.refreshToken(refreshToken);
            session.setAttribute("token", response.getToken());
            session.setAttribute("refreshToken", response.getRefreshToken());
            log.info("Token refreshed for user: {}", session.getAttribute("username"));
            redirectAttributes.addFlashAttribute("successMessage", "Session refreshed successfully.");
            return "redirect:/";
        } catch (Exception e) {
            log.warn("Refresh token failed: {}", e.getMessage());
            session.invalidate();
            redirectAttributes.addFlashAttribute("errorMessage", "Session expired. Please login again.");
            return "redirect:/account/authentication";
        }
    }
}