package com.packshop.client.modules.client.home.controllers;

import java.io.IOException;
import java.util.Set;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.packshop.client.common.utilities.ViewRenderer;
import com.packshop.client.dto.identity.AuthRequest;
import com.packshop.client.dto.identity.AuthResponse;
import com.packshop.client.dto.identity.SignupRequest;
import com.packshop.client.dto.identity.UpdateAccountRequest;
import com.packshop.client.modules.client.home.services.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/account")
@Slf4j
public class AccountController {

    private static final String AUTH_VIEW = "client/account/authentication/index";
    private static final String REDIRECT_HOME = "redirect:/";
    private static final String REDIRECT_AUTH = "redirect:/account/authentication";
    private static final String LOGIN_SUCCESS_MSG = "Login successful";
    private static final String REGISTER_SUCCESS_MSG = "User registered successfully";
    private static final String LOGOUT_SUCCESS_MSG = "You have been logged out.";
    private static final String REFRESH_SUCCESS_MSG = "Session refreshed successfully.";
    private static final String SESSION_EXPIRED_MSG = "Session expired. Please login again.";

    private final ViewRenderer viewRenderer;
    private final ModelMapper modelMapper;
    private final AuthService authService;

    public AccountController(AuthService authService, ViewRenderer viewRenderer,
            ModelMapper modelMapper) {
        this.authService = authService;
        this.viewRenderer = viewRenderer;
        this.modelMapper = modelMapper;
    }

    @GetMapping("/authentication")
    public String authentication(Model model) {
        model.addAttribute("loginRequest", new AuthRequest());
        model.addAttribute("registerRequest", new SignupRequest());
        return viewRenderer.renderView(model, AUTH_VIEW, "Authentication");
    }

    @PostMapping("/login")
    public String login(@ModelAttribute("loginRequest") @Valid AuthRequest request,
            BindingResult result, HttpSession session, RedirectAttributes redirectAttributes) {
        log.debug("Login attempt for user: {}", request.getUsername());

        if (hasValidationErrors(result, redirectAttributes)) {
            return REDIRECT_AUTH;
        }

        AuthResponse response = authService.login(request);
        return handleAuthResponse(response, session, redirectAttributes, request.getUsername(),
                LOGIN_SUCCESS_MSG);
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("registerRequest") @Valid SignupRequest request,
            BindingResult result, RedirectAttributes redirectAttributes) throws IOException {
        log.debug("Register attempt for user: {}", request.getUsername());

        if (hasValidationErrors(result, redirectAttributes)) {
            redirectAttributes.addFlashAttribute("registerRequest", request);
            return REDIRECT_AUTH;
        }

        AuthResponse response = authService.register(request);
        return handleAuthResponse(response, null, redirectAttributes, request.getUsername(),
                REGISTER_SUCCESS_MSG);
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        log.info("User logged out: {}", session.getAttribute("username"));
        session.invalidate();
        redirectAttributes.addFlashAttribute("successMessage", LOGOUT_SUCCESS_MSG);
        return REDIRECT_AUTH;
    }

    @PostMapping("/refresh")
    public String refreshToken(HttpSession session, RedirectAttributes redirectAttributes) {
        String refreshToken = (String) session.getAttribute("refreshToken");
        if (refreshToken == null) {
            log.warn("No refresh token found in session");
            redirectAttributes.addFlashAttribute("errorMessage", "Please login again.");
            return REDIRECT_AUTH;
        }

        AuthResponse response = authService.refreshToken(refreshToken);
        if ("Token refreshed successfully".equals(response.getMessage())) {
            session.setAttribute("token", response.getToken());
            session.setAttribute("refreshToken", response.getRefreshToken());
            log.info("Token refreshed for user: {}", session.getAttribute("username"));
            redirectAttributes.addFlashAttribute("successMessage", REFRESH_SUCCESS_MSG);
            return REDIRECT_HOME;
        } else {
            log.warn("Refresh token failed: {}", response.getMessage());
            session.invalidate();
            redirectAttributes.addFlashAttribute("errorMessage", SESSION_EXPIRED_MSG);
            return REDIRECT_AUTH;
        }
    }

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            return REDIRECT_AUTH;
        }

        AuthResponse userInfo = authService.getCurrentUser(token);

        UpdateAccountRequest updateProfileRequest =
                modelMapper.map(userInfo, UpdateAccountRequest.class);

        model.addAttribute("isLoggedIn", true);
        model.addAttribute("userInfo", userInfo);
        model.addAttribute("user", session);
        model.addAttribute("updateProfileRequest", updateProfileRequest);

        log.info("User info: {}", userInfo);
        return viewRenderer.renderView(model, "client/account/profile/index", "Profile");
    }

    @PostMapping(value = "/profile/update")
    public String updateProfile(
            @ModelAttribute("updateProfileRequest") @Valid UpdateAccountRequest request,
            BindingResult result, HttpSession session, RedirectAttributes redirectAttributes) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please login again.");
            return REDIRECT_AUTH;
        }

        if (hasValidationErrors(result, redirectAttributes)) {
            redirectAttributes.addFlashAttribute("updateProfileRequest", request);
            return "redirect:/account/profile";
        }

        try {
            String username = (String) session.getAttribute("username");
            AuthResponse response = authService.updateProfile(request, username);

            if ("Profile updated successfully".equals(response.getMessage())) {
                storeSessionAttributes(session, response);
                redirectAttributes.addFlashAttribute("successMessage",
                        "Profile updated successfully");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", response.getMessage());
            }
            return "redirect:/account/profile";
        } catch (Exception e) {
            log.error("Failed to update profile for user: {}", session.getAttribute("username"), e);
            String errorMessage = e.getMessage();
            if (e.getCause() instanceof HttpServerErrorException) {
                HttpServerErrorException serverError = (HttpServerErrorException) e.getCause();
                try {
                    AuthResponse errorResponse = new ObjectMapper()
                            .readValue(serverError.getResponseBodyAsString(), AuthResponse.class);
                    errorMessage = errorResponse.getMessage();
                } catch (Exception parseEx) {
                    errorMessage = "Server error: " + serverError.getStatusText();
                }
            } else if (e instanceof IOException) {
                errorMessage = "Failed to communicate with server";
            }
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            redirectAttributes.addFlashAttribute("updateProfileRequest", request);
            return "redirect:/account/profile";
        }
    }

    private boolean hasValidationErrors(BindingResult result,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            String errorMessage = result.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .reduce((msg1, msg2) -> msg1 + "; " + msg2).orElse("Validation failed");
            log.warn("Validation failed: {}", errorMessage);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            return true;
        }
        return false;
    }

    private String handleAuthResponse(AuthResponse response, HttpSession session,
            RedirectAttributes redirectAttributes, String username, String successMsg) {
        log.debug("Response message: {}", response.getMessage());

        if (response.getMessage() != null && successMsg.equals(response.getMessage())) {
            if (session != null && LOGIN_SUCCESS_MSG.equals(successMsg)) {
                storeSessionAttributes(session, response);
            }
            log.info("{} for user: {}", successMsg, username);
            redirectAttributes.addFlashAttribute("successMessage",
                    REGISTER_SUCCESS_MSG.equals(successMsg)
                            ? "Registration successful. Please login."
                            : response.getMessage());

            return LOGIN_SUCCESS_MSG.equals(successMsg) ? REDIRECT_HOME : REDIRECT_AUTH;
        } else {
            log.warn("Failed for user: {} - {}", username, response.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", response.getMessage());
            return REDIRECT_AUTH;
        }
    }

    private void storeSessionAttributes(HttpSession session, AuthResponse response) {
        session.setAttribute("userid", response.getUserId());
        session.setAttribute("username", response.getUsername());
        session.setAttribute("email", response.getEmail());
        session.setAttribute("fullName", response.getFullName());
        session.setAttribute("phoneNumber", response.getPhoneNumber());
        session.setAttribute("avatarUrl", response.getAvatarUrl());
        session.setAttribute("roles", response.getRoles());
        session.setAttribute("token", response.getToken());
        session.setAttribute("refreshToken", response.getRefreshToken());

        Set<String> roles = response.getRoles();
        if (roles != null && roles.contains("ADMIN")) {
            session.setAttribute("isAdmin", true);
        }
    }
}
