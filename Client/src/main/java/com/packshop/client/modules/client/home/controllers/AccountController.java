package com.packshop.client.modules.client.home.controllers;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.packshop.client.common.exceptions.ApiException;
import com.packshop.client.common.utilities.ViewRenderer;
import com.packshop.client.dto.identity.AuthResponse;
import com.packshop.client.dto.identity.UpdateAccountRequest;
import com.packshop.client.dto.identity.UpdatePasswordRequest;
import com.packshop.client.dto.shopping.address.AddressDTO;
import com.packshop.client.modules.client.home.services.AuthService;
import com.packshop.client.modules.client.shopping.address.service.AddressService;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

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
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping
    public String profile(HttpSession session, Model model) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            log.warn("No session token found; redirecting to authentication page.");
            return REDIRECT_AUTH;
        }

        try {
            AuthResponse userInfo = authService.getCurrentUser(token);
            UpdateAccountRequest updateProfileRequest = modelMapper.map(userInfo, UpdateAccountRequest.class);
            addProfileAttributesToModel(model, userInfo, updateProfileRequest);

            List<AddressDTO> addresses = addressService.getUserAddresses(userInfo.getUserId());
            addresses.sort(Comparator.comparing(AddressDTO::getIsDefault).reversed());
            model.addAttribute("addresses", addresses);
            model.addAttribute("newAddress", new AddressDTO());

            log.debug("Loaded profile for user ID: {} with {} addresses.", userInfo.getUserId(), addresses.size());
            return viewRenderer.renderView(model, PROFILE_VIEW, "Profile");
        } catch (Exception e) {
            log.error("Failed to load profile page for token: {}. Error: {}", token, e.getMessage(), e);
            return REDIRECT_AUTH;
        }
    }

    @PostMapping("/add-address")
    public String addAddress(@Valid @ModelAttribute("newAddress") AddressDTO newAddress,
            BindingResult result,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            log.warn("Session token missing during address addition; redirecting to authentication.");
            return REDIRECT_AUTH;
        }

        AuthResponse userInfo = authService.getCurrentUser(token);
        if (result.hasErrors()) {
            log.warn("Validation failed for new address for user ID: {}. Errors: {}", userInfo.getUserId(),
                    result.getAllErrors());
            redirectAttributes.addFlashAttribute("newAddress", newAddress);
            return REDIRECT_PROFILE;
        }

        try {
            addressService.saveAddress(newAddress, userInfo.getUserId());
            log.info("Address added successfully for user ID: {}.", userInfo.getUserId());
            redirectAttributes.addFlashAttribute("successMessage", "Address added successfully!");
        } catch (ApiException e) {
            log.error("Failed to add address for user ID: {}. Error: {}", userInfo.getUserId(), e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return REDIRECT_PROFILE;
    }

    @PostMapping("/remove-address/{addressId}")
    public String removeAddress(@PathVariable("addressId") Long addressId,
            RedirectAttributes redirectAttributes) {
        try {
            addressService.removeAddress(addressId);
            log.info("Address ID: {} removed successfully.", addressId);
            redirectAttributes.addFlashAttribute("successMessage", "Address removed successfully!");
        } catch (ApiException e) {
            log.error("Failed to remove address ID: {}. Error: {}", addressId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to remove address.");
        }
        return REDIRECT_PROFILE;
    }

    @PostMapping("/update")
    public String updateProfile(@Valid @ModelAttribute("updateProfileRequest") UpdateAccountRequest request,
            BindingResult result,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            log.warn("Session token missing during profile update; redirecting to authentication.");
            redirectAttributes.addFlashAttribute("errorMessage", SESSION_EXPIRED_MSG);
            return REDIRECT_AUTH;
        }

        if (hasValidationErrors(result, redirectAttributes)) {
            redirectAttributes.addFlashAttribute("updateProfileRequest", request);
            return REDIRECT_PROFILE;
        }

        String username = (String) session.getAttribute("username");
        try {
            request.setToken(token);
            AuthResponse response = authService.updateProfile(request, username);
            if (UPDATE_PROFILE_SUCCESS_MSG.equals(response.getMessage())) {
                updateSessionAttributes(session, response);
                log.info("Profile updated successfully for username: {}.", username);
                redirectAttributes.addFlashAttribute("successMessage", UPDATE_PROFILE_SUCCESS_MSG);
            } else {
                log.warn("Profile update failed for username: {}. Reason: {}", username, response.getMessage());
                redirectAttributes.addFlashAttribute("errorMessage", response.getMessage());
                redirectAttributes.addFlashAttribute("updateProfileRequest", request);
            }
        } catch (Exception e) {
            log.error("Error updating profile for username: {}. Error: {}", username, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e, "Failed to update profile"));
            redirectAttributes.addFlashAttribute("updateProfileRequest", request);
        }
        return REDIRECT_PROFILE;
    }

    @PostMapping("/update-password")
    public String updatePassword(@Valid @ModelAttribute("updatePasswordRequest") UpdatePasswordRequest request,
            BindingResult result,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            log.warn("Session token missing during password update; redirecting to authentication.");
            redirectAttributes.addFlashAttribute("errorMessage", SESSION_EXPIRED_MSG);
            return REDIRECT_AUTH;
        }

        if (hasValidationErrors(result, redirectAttributes)) {
            redirectAttributes.addFlashAttribute("updatePasswordRequest", request);
            return REDIRECT_PROFILE;
        }

        String username = (String) session.getAttribute("username");
        try {
            AuthResponse response = authService.updatePassword(request.getOldPassword(), request.getNewPassword());
            if (UPDATE_PASSWORD_SUCCESS_MSG.equals(response.getMessage())) {
                log.info("Password updated successfully for username: {}.", username);
                redirectAttributes.addFlashAttribute("successMessage", UPDATE_PASSWORD_SUCCESS_MSG);
            } else {
                log.warn("Password update failed for username: {}. Reason: {}", username, response.getMessage());
                redirectAttributes.addFlashAttribute("errorMessage", response.getMessage());
                redirectAttributes.addFlashAttribute("updatePasswordRequest", request);
            }
        } catch (Exception e) {
            log.error("Error updating password for username: {}. Error: {}", username, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e, "Failed to update password"));
        }
        return REDIRECT_PROFILE;
    }

    private boolean hasValidationErrors(BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            String errorMessage = result.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .reduce((msg1, msg2) -> msg1 + "; " + msg2)
                    .orElse("Validation failed");
            log.warn("Validation errors occurred: {}", errorMessage);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            return true;
        }
        return false;
    }

    private String extractErrorMessage(Exception e, String defaultMessage) {
        if (e instanceof HttpClientErrorException || e.getCause() instanceof HttpClientErrorException) {
            HttpClientErrorException clientError = (e instanceof HttpClientErrorException)
                    ? (HttpClientErrorException) e
                    : (HttpClientErrorException) e.getCause();
            try {
                AuthResponse errorResponse = objectMapper.readValue(clientError.getResponseBodyAsString(),
                        AuthResponse.class);
                if (errorResponse != null && errorResponse.getMessage() != null) {
                    return errorResponse.getMessage();
                }
            } catch (Exception parseEx) {
                log.warn("Failed to parse error response from API: {}", parseEx.getMessage());
            }
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