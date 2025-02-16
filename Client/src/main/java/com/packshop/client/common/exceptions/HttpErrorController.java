package com.packshop.client.common.exceptions;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class HttpErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            model.addAttribute("statusCode", statusCode);

            switch (statusCode) {
                case 404 -> {
                    model.addAttribute("errorMessage", "Page not found!");
                    return "error/index";
                }
                default -> {
                    model.addAttribute("errorMessage", "An unexpected error occurred.");
                    return "error/index";
                }
            }
        }
        return "error/index";
    }
}
