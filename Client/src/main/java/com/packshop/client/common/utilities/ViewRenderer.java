package com.packshop.client.common.utilities;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import com.packshop.client.dto.identity.AuthResponse;
import com.packshop.client.modules.client.home.services.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Component
public class ViewRenderer {

    private static final String CLIENT_LAYOUT = "fragments/client/_layout";
    private static final String DASHBOARD_LAYOUT = "fragments/dashboard/_layout";

    private final AuthService authService;

    @Autowired
    private HttpServletRequest request;

    public ViewRenderer(AuthService authService) {
        this.authService = authService;
    }

    @SuppressWarnings("null")
    public String renderView(Model model, String templateName, String title) {
        HttpSession session = request.getSession(false);

        String token = (session != null) ? (String) session.getAttribute("token") : null;

        if (token != null) {
            try {
                AuthResponse userInfo = authService.getCurrentUser(token);
                model.addAttribute("username", userInfo.getUsername());
                model.addAttribute("roles", userInfo.getRoles());
                model.addAttribute("isLoggedIn", true);

                if (templateName.contains("dashboard")) {
                    Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
                    if (isAdmin == null || !isAdmin) {
                        model.addAttribute("statusCode", 403);
                        model.addAttribute("errorTitle", "Forbidden");
                        model.addAttribute("errorMessage", "You don't have permission to access this resource.");
                        return "error/index";
                    }
                }
            } catch (Exception e) {
                session.invalidate();
                model.addAttribute("isLoggedIn", false);
            }
        } else {
            model.addAttribute("isLoggedIn", false);
            if (templateName.contains("dashboard")) {
                model.addAttribute("statusCode", 403);
                model.addAttribute("errorTitle", "Forbidden");
                model.addAttribute("errorMessage", "You need to log in with proper permissions.");
                return "error/index";
            }
        }

        model.addAttribute("view", templateName);
        model.addAttribute("title", title);

        if (templateName.contains("dashboard")) {
            return DASHBOARD_LAYOUT;
        }
        return CLIENT_LAYOUT;
    }
}