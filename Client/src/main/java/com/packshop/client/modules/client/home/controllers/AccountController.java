package com.packshop.client.modules.client.home.controllers;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.packshop.client.common.utilities.ViewRenderer;
import com.packshop.client.dto.identity.AuthResponse;
import com.packshop.client.dto.identity.UpdateAccountRequest;
import com.packshop.client.dto.identity.UpdatePasswordRequest;
import com.packshop.client.dto.shopping.address.AddressDTO;
import com.packshop.client.modules.client.home.services.AuthService;
import com.packshop.client.modules.client.shopping.address.service.AddressService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/account/profile")
public class AccountController {

    private static final String PROFILE_VIEW = "client/account/profile/index";
    private static final String REDIRECT_AUTH = "redirect:/account/authentication";
    private static final String REDIRECT_PROFILE = "redirect:/account/profile";
    private static final String UPDATE_PROFILE_SUCCESS_MSG = "Profile updated successfully";
    private static final String UPDATE_PASSWORD_SUCCESS_MSG = "Password updated successfully";
    private static final String SESSION_EXPIRED_MSG = "Session expired. Please login again.";

    private final ViewRenderer viewRenderer;
    private final ModelMapper modelMapper;
    private final AuthService authService;
    private final AddressService addressService;

    @GetMapping()
    public String profile(HttpSession session, Model model) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            return REDIRECT_AUTH;
        }
        AuthResponse userInfo = authService.getCurrentUser(token);
        UpdateAccountRequest updateProfileRequest = modelMapper.map(userInfo, UpdateAccountRequest.class);
        addProfileAttributesToModel(model, userInfo, updateProfileRequest);

        List<AddressDTO> addresses = addressService.getUserAddresses(userInfo.getUserId());
        addresses.sort(Comparator.comparing(AddressDTO::getIsDefault).reversed());

        model.addAttribute("addresses", addresses);

        log.info("User info: {}", userInfo);
        return viewRenderer.renderView(model, PROFILE_VIEW, "Profile");
    }

    @PostMapping("/update")
    public String updateProfile(
            @ModelAttribute("updateProfileRequest") @Valid UpdateAccountRequest request,
            BindingResult result, HttpSession session, RedirectAttributes redirectAttributes) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            redirectAttributes.addFlashAttribute("errorMessage", SESSION_EXPIRED_MSG);
            return REDIRECT_AUTH;
        }
        if (hasValidationErrors(result, redirectAttributes)) {
            redirectAttributes.addFlashAttribute("updateProfileRequest", request);
            return REDIRECT_PROFILE;
        }

        try {
            String username = (String) session.getAttribute("username");
            request.setToken(token);
            AuthResponse response = authService.updateProfile(request, username);
            if (UPDATE_PROFILE_SUCCESS_MSG.equals(response.getMessage())) {
                updateSessionAttributes(session, response);
                log.info("Profile updated successfully for user: {}", username);
                redirectAttributes.addFlashAttribute("successMessage", UPDATE_PROFILE_SUCCESS_MSG);
                return REDIRECT_PROFILE;
            } else {
                log.warn("Failed to update profile for user: {} - {}", username, response.getMessage());
                redirectAttributes.addFlashAttribute("errorMessage", response.getMessage());
                redirectAttributes.addFlashAttribute("updateProfileRequest", request);
                return REDIRECT_PROFILE;
            }
        } catch (Exception e) {
            log.error("Error updating profile: ", e);
            String errorMessage = extractErrorMessage(e, "Failed to update profile");
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            redirectAttributes.addFlashAttribute("updateProfileRequest", request);
            return REDIRECT_PROFILE;
        }
    }

    @PostMapping("/update-password")
    public String updatePassword(
            @ModelAttribute("updatePasswordRequest") @Valid UpdatePasswordRequest request,
            BindingResult result, HttpSession session, RedirectAttributes redirectAttributes) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            redirectAttributes.addFlashAttribute("errorMessage", SESSION_EXPIRED_MSG);
            return REDIRECT_AUTH;
        }
        if (hasValidationErrors(result, redirectAttributes)) {
            return REDIRECT_PROFILE;
        }

        try {
            String username = (String) session.getAttribute("username");
            AuthResponse response = authService.updatePassword(request.getOldPassword(), request.getNewPassword());
            if (UPDATE_PASSWORD_SUCCESS_MSG.equals(response.getMessage())) {
                log.info("Password updated successfully for user: {}", username);
                redirectAttributes.addFlashAttribute("successMessage", UPDATE_PASSWORD_SUCCESS_MSG);
                return REDIRECT_PROFILE;
            } else {
                log.warn("Failed to update password for user: {} - {}", username, response.getMessage());
                redirectAttributes.addFlashAttribute("errorMessage", response.getMessage());
                redirectAttributes.addFlashAttribute("updatePasswordRequest", request);
                return REDIRECT_PROFILE;
            }
        } catch (Exception e) {
            log.error("Error updating password: ", e);
            String errorMessage = extractErrorMessage(e, "Failed to update password");
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            return REDIRECT_PROFILE;
        }
    }

    private boolean hasValidationErrors(BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            String errorMessage = result.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .reduce((msg1, msg2) -> msg1 + "; " + msg2)
                    .orElse("Validation failed");
            log.warn("Validation failed: {}", errorMessage);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            return true;
        }
        return false;
    }

    private String extractErrorMessage(Exception e, String defaultMessage) {
        String errorMessage = defaultMessage;
        if (e instanceof HttpClientErrorException) {
            HttpClientErrorException clientError = (HttpClientErrorException) e;
            errorMessage = parseErrorResponse(clientError, errorMessage);
        } else if (e.getCause() instanceof HttpClientErrorException) {
            HttpClientErrorException clientError = (HttpClientErrorException) e.getCause();
            errorMessage = parseErrorResponse(clientError, errorMessage);
        }
        return errorMessage;
    }

    private String parseErrorResponse(HttpClientErrorException clientError, String defaultMessage) {
        try {
            AuthResponse errorResponse = new ObjectMapper()
                    .readValue(clientError.getResponseBodyAsString(), AuthResponse.class);
            if (errorResponse != null && errorResponse.getMessage() != null) {
                return errorResponse.getMessage();
            }
        } catch (Exception parseEx) {
            log.warn("Failed to parse error response: {}", parseEx.getMessage());
        }
        return defaultMessage;
    }

    private void updateSessionAttributes(HttpSession session, AuthResponse response) {
        session.setAttribute("email", response.getEmail());
        session.setAttribute("fullName", response.getFullName());
        session.setAttribute("phoneNumber", response.getPhoneNumber());
        session.setAttribute("avatarUrl", response.getAvatarUrl());

        if (response.getToken() != null) {
            session.setAttribute("token", response.getToken());
        }

        if (response.getRefreshToken() != null) {
            session.setAttribute("refreshToken", response.getRefreshToken());
        }

        Set<String> roles = response.getRoles();
        if (roles != null) {
            session.setAttribute("roles", roles);
            session.setAttribute("isAdmin", roles.contains("ADMIN"));
        }
    }

    private void addProfileAttributesToModel(Model model, AuthResponse userInfo,
            UpdateAccountRequest updateProfileRequest) {
        model.addAttribute("isLoggedIn", true);
        model.addAttribute("userInfo", userInfo);
        model.addAttribute("updateProfileRequest", updateProfileRequest);
        model.addAttribute("updatePasswordRequest", new UpdatePasswordRequest());
    }
}