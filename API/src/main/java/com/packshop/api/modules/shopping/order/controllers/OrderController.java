package com.packshop.api.modules.shopping.order.controllers;

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

import com.packshop.api.common.exceptions.ResourceNotFoundException;
import com.packshop.api.modules.identity.entities.User;
import com.packshop.api.modules.shopping.order.dto.OrderDTO;
import com.packshop.api.modules.shopping.order.entities.Order;
import com.packshop.api.modules.shopping.order.services.OrderService;

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

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDTO> getOrderById(
            @PathVariable Long orderId,
            @AuthenticationPrincipal User user) {
        log.info("Retrieving order {} for user {}", orderId, user.getUsername());
        OrderDTO order = orderService.getOrderById(orderId, user);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/create-with-momo")
    public ResponseEntity<OrderDTO> createOrderFromCartWithMoMo(
            @RequestParam("address") Long addressId,
            @AuthenticationPrincipal User user) {
        try {
            if (user == null) {
                log.warn("Unauthenticated user attempted to create an order");
                return ResponseEntity.status(401).body(null); // Unauthorized
            }

            OrderDTO orderDTO = orderService.createOrderFromCartWithMoMo(user, addressId);

            log.info("Order created successfully with MoMo for user: {}", user.getUsername());
            return ResponseEntity.ok(orderDTO);

        } catch (ResourceNotFoundException e) {
            log.error("Resource not found: {}", e.getMessage());
            return ResponseEntity.status(404).body(null);
        } catch (IllegalStateException e) {
            log.error("Illegal state: {}", e.getMessage());
            return ResponseEntity.status(400).body(null);
        } catch (Exception e) {
            log.error("Error creating order with MoMo: {}", e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(
            @RequestParam("address") Long addressId,
            @AuthenticationPrincipal User user) {
        log.info("Creating order for user: {}", user.getUsername());
        OrderDTO createdOrder = orderService.createOrderFromCart(user, addressId);
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
