package com.packshop.client.common.utilities;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import com.packshop.client.common.services.AuthService;
import com.packshop.client.dto.identity.AuthResponse;

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
        HttpSession session = request.getSession(false);  // Lấy session nếu có

        String token = (session != null) ? (String) session.getAttribute("token") : null;

        if (token != null) {
            try {
                AuthResponse userInfo = authService.getCurrentUser(token);
                model.addAttribute("username", userInfo.getUsername());
                model.addAttribute("roles", userInfo.getRoles());
                model.addAttribute("isLoggedIn", true);
            } catch (Exception e) {
                session.invalidate();
                model.addAttribute("isLoggedIn", false);
            }
        } else {
            model.addAttribute("isLoggedIn", false);
        }

        model.addAttribute("view", templateName);
        model.addAttribute("title", title);

        if (templateName.contains("dashboard")) {
            return DASHBOARD_LAYOUT;
        }
        return CLIENT_LAYOUT;
    }
}
