package com.packshop.client.modules.client.shopping.order.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.packshop.client.common.exceptions.ApiException;
import com.packshop.client.common.utilities.ViewRenderer;
import com.packshop.client.dto.shopping.order.OrderDTO;
import com.packshop.client.modules.client.shopping.order.service.OrderService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

  private static final String REDIRECT_TO_ORDER = "redirect:/order";

  private final ViewRenderer viewRenderer;
  private final OrderService orderService;

  @GetMapping
  public String showOrders(HttpSession session, Model model) {
    String token = (String) session.getAttribute("token");
    if (token == null) {
      return "redirect:/account/authentication";
    }
    try {
      Long userId = (Long) model.getAttribute("userId");
      List<OrderDTO> orders = orderService.getUserOrders(userId);
      model.addAttribute("orders", orders);
    } catch (ApiException e) {
      log.error("Error fetching order: {}", e.getMessage());
      model.addAttribute("errorMessage", "Unable to load orders. Please try again later.");
    }
    return viewRenderer.renderView(model, "client/order/index", "Order");
  }

  @PostMapping("/{orderId}/update-status")
  public String updateOrderStatus(@PathVariable Long orderId, @RequestParam String newStatus, HttpSession session,
      RedirectAttributes redirectAttributes, Model model) {
    String token = (String) session.getAttribute("token");
    if (token == null) {
      return "redirect:/account/authentication";
    }
    try {
      orderService.updateOrderStatus(orderId, newStatus);
      redirectAttributes.addFlashAttribute("successMessage", "Order status updated successfully.");
      return "redirect:/order";
    } catch (ApiException e) {
      log.error("Error updating order status: {}", e.getMessage());
      redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
      return REDIRECT_TO_ORDER;
    }
  }

  @PostMapping("/{orderId}/cancel")
  public String cancelOrder(@PathVariable Long orderId, HttpSession session,
      RedirectAttributes redirectAttributes, Model model) {
    String token = (String) session.getAttribute("token");
    if (token == null) {
      return "redirect:/account/authentication";
    }
    try {
      orderService.cancelOrder(orderId);
      redirectAttributes.addFlashAttribute("successMessage", "Order cancelled successfully.");
      return "redirect:/order";
    } catch (ApiException e) {
      log.error("Error cancelling order: {}", e.getMessage());
      redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
      return REDIRECT_TO_ORDER;
    }
  }
}
