package com.packshop.client.common.exceptions;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class HttpErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        String requestUri = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            log.info("Handling error - Status Code: {}, Request URI: {}", statusCode, requestUri);

            String errorMessage = (message != null && !message.toString().isEmpty())
                    ? message.toString()
                    : getErrorMessage(statusCode);

            model.addAttribute("statusCode", statusCode);
            model.addAttribute("errorMessage", errorMessage);

            switch (statusCode) {
                case 400 -> {
                    log.warn("Bad Request error occurred for URI: {}", requestUri);
                    model.addAttribute("errorTitle", "Bad Request");
                    return "error/index";
                }
                case 403 -> {
                    log.warn("Forbidden error occurred for URI: {}", requestUri);
                    model.addAttribute("errorTitle", "Forbidden");
                    return "error/index";
                }
                case 404 -> {
                    log.warn("Not Found error occurred for URI: {}", requestUri);
                    model.addAttribute("errorTitle", "Not Found");
                    return "error/index";
                }
                case 500 -> {
                    log.error("Server Error occurred for URI: {}, Message: {}", requestUri, errorMessage);
                    model.addAttribute("errorTitle", "Server Error");
                    return "error/index";
                }
                default -> {
                    log.error("Unexpected error occurred - Status Code: {}, URI: {}", statusCode, requestUri);
                    model.addAttribute("errorTitle", "Unexpected Error");
                    return "error/index";
                }
            }
        }

        // Trường hợp không có mã trạng thái
        log.error("Unknown error occurred for URI: {}", requestUri);
        model.addAttribute("statusCode", "Unknown");
        model.addAttribute("errorTitle", "Something Went Wrong");
        model.addAttribute("errorMessage", "An unexpected error occurred.");
        return "error/index";
    }

    private String getErrorMessage(int statusCode) {
        return switch (statusCode) {
            case 400 -> "The request was invalid or cannot be processed.";
            case 403 -> "You don't have permission to access this resource.";
            case 404 -> "The page or resource you are looking for cannot be found.";
            case 500 -> "Something went wrong on our end. Please try again later.";
            default -> "An unexpected error occurred.";
        };
    }
}