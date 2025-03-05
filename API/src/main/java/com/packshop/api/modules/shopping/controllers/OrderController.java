package com.packshop.api.modules.shopping.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.packshop.api.modules.identity.entities.User;
import com.packshop.api.modules.shopping.dto.order.OrderDTO;
import com.packshop.api.modules.shopping.entities.order.Order;
import com.packshop.api.modules.shopping.services.OrderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<List<OrderDTO>> getUserOrders(@AuthenticationPrincipal User user) {
        log.info("Retrieving orders for user: {}", user.getUsername());
        List<OrderDTO> orders = orderService.getOrdersByUser(user);
        return ResponseEntity.ok(orders);
    }

    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(
            @AuthenticationPrincipal User user) {
        log.info("Creating order for user: {}", user.getUsername());
        OrderDTO createdOrder = orderService.createOrderFromCart(user);
        return ResponseEntity.ok(createdOrder);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam Order.Status newStatus,
            @AuthenticationPrincipal User user) {
        log.info("Updating order {} status to {} for user {}",
                orderId, newStatus, user.getUsername());
        OrderDTO updatedOrder = orderService.updateOrderStatus(orderId, newStatus, user);
        return ResponseEntity.ok(updatedOrder);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<OrderDTO> cancelOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal User user) {
        log.info("Cancelling order {} for user {}", orderId, user.getUsername());
        orderService.cancelOrder(orderId, user);
        return ResponseEntity.noContent().build();
    }
}
